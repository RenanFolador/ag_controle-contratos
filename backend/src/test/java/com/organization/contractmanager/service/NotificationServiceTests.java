package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractAssignment;
import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock private NotificationRepository notificationRepository;
    @Mock private ContractAssignmentRepository assignmentRepository;
    @Mock private NotificationProvider emailProvider;
    @Mock private ContractHistoryService historyService;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        when(emailProvider.getChannel()).thenReturn(NotificationChannel.EMAIL);
        service = new NotificationService(
                notificationRepository, assignmentRepository, List.of(emailProvider),
                historyService,
                Clock.fixed(Instant.parse("2026-08-12T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void createsEmailAndWhatsappForActiveResponsibleWithAvailableAddresses() {
        Contract contract = contract();
        Person person = person("responsavel@example.com", "11999999999", true);
        ContractAssignment assignment = assignment(contract, person, ContractRole.MANAGER);
        NotificationSchedule schedule = schedule(contract);
        when(assignmentRepository.findActiveResponsibleAssignments(contract.getId()))
                .thenReturn(List.of(assignment));
        when(notificationRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var notifications = service.createForSchedule(schedule);

        assertThat(notifications).hasSize(2);
        assertThat(notifications).extracting(notification -> notification.getChannel())
                .containsExactly(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP);
        assertThat(notifications).allSatisfy(notification -> {
            assertThat(notification.getExpirationDate()).isEqualTo(schedule.getExpirationDate());
            assertThat(notification.getDaysBefore()).isEqualTo(schedule.getDaysBefore());
            assertThat(notification.getPerson()).isSameAs(person);
        });
        assertThat(notifications.getFirst().getSubject()).isEqualTo(
                "[Contratos] Aviso de vencimento — Contrato NOTIFY-SERVICE — prazo de 30 dias");
    }

    @Test
    void deduplicatesPersonWithMultipleActiveRolesAndExistingDelivery() {
        Contract contract = contract();
        Person person = person("responsavel@example.com", null, false);
        NotificationSchedule schedule = schedule(contract);
        when(assignmentRepository.findActiveResponsibleAssignments(contract.getId()))
                .thenReturn(List.of(
                        assignment(contract, person, ContractRole.MANAGER),
                        assignment(contract, person, ContractRole.PRIMARY_INSPECTOR)));
        when(notificationRepository.existsForDelivery(
                contract.getId(), person.getId(), schedule.getExpirationDate(),
                schedule.getDaysBefore(), NotificationChannel.EMAIL)).thenReturn(true);
        when(notificationRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var notifications = service.createForSchedule(schedule);

        assertThat(notifications).isEmpty();
    }

    @Test
    void sendsUsingChannelProviderAndRecordsSuccessfulAttempt() {
        Contract contract = contract();
        Person person = person("responsavel@example.com", null, false);
        NotificationSchedule schedule = schedule(contract);
        when(assignmentRepository.findActiveResponsibleAssignments(contract.getId()))
                .thenReturn(List.of(assignment(contract, person, ContractRole.MANAGER)));
        when(notificationRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var notifications = service.createAndSendForSchedule(schedule);

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.getStatus().name()).isEqualTo("SENT");
            assertThat(notification.getSentAt()).isEqualTo(
                    Instant.parse("2026-08-12T12:00:00Z"));
            assertThat(notification.getRetryCount()).isZero();
            assertThat(notification.getErrorMessage()).isNull();
            verify(emailProvider).send(notification);
            verify(historyService).record(
                    org.mockito.ArgumentMatchers.eq(contract.getId()),
                    org.mockito.ArgumentMatchers.eq("system"),
                    org.mockito.ArgumentMatchers.eq("NOTIFICATION"),
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.eq(HistoryAction.NOTIFICATION_SENT),
                    org.mockito.ArgumentMatchers.eq("status=PENDING"),
                    org.mockito.ArgumentMatchers.contains("status=SENT"));
        });
    }

    @Test
    void recordsProviderFailureAndRetryCountWithoutThrowing() {
        Contract contract = contract();
        Person person = person("responsavel@example.com", null, false);
        NotificationSchedule schedule = schedule(contract);
        when(assignmentRepository.findActiveResponsibleAssignments(contract.getId()))
                .thenReturn(List.of(assignment(contract, person, ContractRole.MANAGER)));
        when(notificationRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailProvider).send(org.mockito.ArgumentMatchers.any());

        var notifications = service.createAndSendForSchedule(schedule);

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.getStatus().name()).isEqualTo("FAILED");
            assertThat(notification.getSentAt()).isNull();
            assertThat(notification.getRetryCount()).isEqualTo(1);
            assertThat(notification.getErrorMessage()).isEqualTo("SMTP unavailable");
        });
        verify(historyService).record(
                org.mockito.ArgumentMatchers.eq(contract.getId()),
                org.mockito.ArgumentMatchers.eq("system"),
                org.mockito.ArgumentMatchers.eq("NOTIFICATION"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(HistoryAction.NOTIFICATION_FAILED),
                org.mockito.ArgumentMatchers.eq("status=PENDING"),
                org.mockito.ArgumentMatchers.contains("status=FAILED"));
    }

    private Contract contract() {
        Contract contract = new Contract(
                "NOTIFY-SERVICE", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "test");
        ReflectionTestUtils.setField(contract, "id", UUID.randomUUID());
        return contract;
    }

    private Person person(String email, String phone, boolean whatsappEnabled) {
        Person person = new Person(
                "Responsavel", null, "REG", email, phone, whatsappEnabled, true);
        ReflectionTestUtils.setField(person, "id", UUID.randomUUID());
        return person;
    }

    private ContractAssignment assignment(
            Contract contract, Person person, ContractRole role) {
        return new ContractAssignment(
                contract, person, role, LocalDate.of(2026, 1, 1), null, "test");
    }

    private NotificationSchedule schedule(Contract contract) {
        return new NotificationSchedule(
                contract, contract.getEndDate(), 30,
                contract.getEndDate().minusDays(30));
    }
}

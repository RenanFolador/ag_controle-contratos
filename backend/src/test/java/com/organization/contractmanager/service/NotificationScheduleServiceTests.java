package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationDeadline;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.repository.NotificationDeadlineRepository;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationScheduleServiceTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 12);

    @Mock
    private NotificationDeadlineRepository deadlineRepository;

    @Mock
    private NotificationScheduleRepository scheduleRepository;

    @Mock
    private NotificationDispatcher dispatcher;

    private NotificationScheduleService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-12T12:00:00Z"), ZoneOffset.UTC);
        service = new NotificationScheduleService(
                deadlineRepository, scheduleRepository, clock, dispatcher);
    }

    @Test
    void createsPendingSchedulesFromEnabledDatabaseConfiguration() {
        Contract contract = contract(ContractStatus.ACTIVE, LocalDate.of(2026, 10, 11));
        when(deadlineRepository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of(
                        new NotificationDeadline(60, true),
                        new NotificationDeadline(30, true),
                        new NotificationDeadline(15, true)));
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationSchedule> schedules = service.createForActiveContract(contract);

        assertThat(schedules).hasSize(3);
        assertThat(schedules).extracting(NotificationSchedule::getScheduledDate)
                .containsExactly(
                        LocalDate.of(2026, 8, 12),
                        LocalDate.of(2026, 9, 11),
                        LocalDate.of(2026, 9, 26));
        assertThat(schedules).allMatch(schedule ->
                schedule.getStatus() == NotificationScheduleStatus.PENDING
                        && schedule.getExpirationDate().equals(LocalDate.of(2026, 10, 11)));
    }

    @Test
    void skipsSchedulesWhoseScheduledDateIsBeforeToday() {
        Contract contract = contract(ContractStatus.ACTIVE, TODAY.plusDays(30));
        when(deadlineRepository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of(
                        new NotificationDeadline(60, true),
                        new NotificationDeadline(30, true),
                        new NotificationDeadline(15, true)));
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationSchedule> schedules = service.createForActiveContract(contract);

        assertThat(schedules).extracting(NotificationSchedule::getDaysBefore)
                .containsExactly(30, 15);
    }

    @Test
    void doesNotCreateSchedulesForNonActiveContract() {
        List<NotificationSchedule> schedules = service.createForActiveContract(
                contract(ContractStatus.CLOSED, TODAY.plusDays(90)));

        assertThat(schedules).isEmpty();
        verify(deadlineRepository, never()).findAllByEnabledTrueOrderByDaysBeforeDesc();
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void extensionCancelsOldPendingSchedulesAndCreatesNewOnes() {
        LocalDate oldExpiration = LocalDate.of(2026, 10, 11);
        LocalDate newExpiration = LocalDate.of(2026, 12, 31);
        Contract contract = contract(ContractStatus.ACTIVE, newExpiration);
        NotificationSchedule oldPending = new NotificationSchedule(
                contract, oldExpiration, 30, oldExpiration.minusDays(30));
        when(scheduleRepository.findPendingByContractIdAndExpirationDate(
                contract.getId(), oldExpiration)).thenReturn(List.of(oldPending));
        when(deadlineRepository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of(new NotificationDeadline(30, true)));
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationSchedule> created = service.rescheduleForExpirationChange(
                contract, oldExpiration);

        assertThat(oldPending.getStatus()).isEqualTo(NotificationScheduleStatus.CANCELLED);
        assertThat(created).singleElement().satisfies(schedule -> {
            assertThat(schedule.getExpirationDate()).isEqualTo(newExpiration);
            assertThat(schedule.getScheduledDate()).isEqualTo(newExpiration.minusDays(30));
            assertThat(schedule.getStatus()).isEqualTo(NotificationScheduleStatus.PENDING);
        });
        verify(scheduleRepository, times(2)).saveAll(anyList());
    }

    @Test
    void reductionCancelsOldPendingAndSkipsNewPastSchedule() {
        LocalDate oldExpiration = TODAY.plusDays(120);
        Contract contract = contract(ContractStatus.ACTIVE, TODAY.plusDays(10));
        NotificationSchedule oldPending = new NotificationSchedule(
                contract, oldExpiration, 30, oldExpiration.minusDays(30));
        when(scheduleRepository.findPendingByContractIdAndExpirationDate(
                contract.getId(), oldExpiration)).thenReturn(List.of(oldPending));
        when(deadlineRepository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of(new NotificationDeadline(30, true)));
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationSchedule> created = service.rescheduleForExpirationChange(
                contract, oldExpiration);

        assertThat(oldPending.getStatus()).isEqualTo(NotificationScheduleStatus.CANCELLED);
        assertThat(created).isEmpty();
    }

    @Test
    void preservesAlreadyProcessedScheduleWhenExpirationChanges() {
        LocalDate oldExpiration = TODAY.plusDays(90);
        Contract contract = contract(ContractStatus.ACTIVE, TODAY.plusDays(120));
        NotificationSchedule processed = new NotificationSchedule(
                contract, oldExpiration, 30, oldExpiration.minusDays(30));
        ReflectionTestUtils.setField(
                processed, "status", NotificationScheduleStatus.PROCESSED);
        ReflectionTestUtils.setField(processed, "processedAt", Instant.now());
        when(scheduleRepository.findPendingByContractIdAndExpirationDate(
                contract.getId(), oldExpiration)).thenReturn(List.of(processed));
        when(deadlineRepository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of());
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.rescheduleForExpirationChange(contract, oldExpiration);

        assertThat(processed.getStatus()).isEqualTo(NotificationScheduleStatus.PROCESSED);
        assertThat(processed.getProcessedAt()).isNotNull();
    }

    @Test
    void processesPendingSchedulesDueTodayOrEarlier() {
        Contract contract = contract(ContractStatus.ACTIVE, TODAY.plusDays(30));
        NotificationSchedule overdue = new NotificationSchedule(
                contract, TODAY.plusDays(30), 60, TODAY.minusDays(30));
        NotificationSchedule dueToday = new NotificationSchedule(
                contract, TODAY.plusDays(30), 30, TODAY);
        when(scheduleRepository.findDueSchedules(
                NotificationScheduleStatus.PENDING, TODAY))
                .thenReturn(List.of(overdue, dueToday));

        int processed = service.processDueSchedules();

        assertThat(processed).isEqualTo(2);
        assertThat(List.of(overdue, dueToday)).allSatisfy(schedule -> {
            assertThat(schedule.getStatus()).isEqualTo(NotificationScheduleStatus.PROCESSED);
            assertThat(schedule.getProcessedAt()).isEqualTo(
                    Instant.parse("2026-08-12T12:00:00Z"));
        });
        verify(dispatcher, times(2)).dispatch(any(NotificationSchedule.class));
        verify(scheduleRepository).saveAll(List.of(overdue, dueToday));
    }

    @Test
    void cancelsDueSchedulesForClosedAndCancelledContractsWithoutDispatching() {
        NotificationSchedule closed = new NotificationSchedule(
                contract(ContractStatus.CLOSED, TODAY), TODAY, 15, TODAY);
        NotificationSchedule cancelled = new NotificationSchedule(
                contract(ContractStatus.CANCELLED, TODAY), TODAY, 15, TODAY);
        when(scheduleRepository.findDueSchedules(
                NotificationScheduleStatus.PENDING, TODAY))
                .thenReturn(List.of(closed, cancelled));

        int processed = service.processDueSchedules();

        assertThat(processed).isZero();
        assertThat(closed.getStatus()).isEqualTo(NotificationScheduleStatus.CANCELLED);
        assertThat(cancelled.getStatus()).isEqualTo(NotificationScheduleStatus.CANCELLED);
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void marksScheduleAsFailedWhenPreparedDispatchFails() {
        NotificationSchedule schedule = new NotificationSchedule(
                contract(ContractStatus.ACTIVE, TODAY), TODAY, 15, TODAY);
        when(scheduleRepository.findDueSchedules(
                NotificationScheduleStatus.PENDING, TODAY)).thenReturn(List.of(schedule));
        org.mockito.Mockito.doThrow(new IllegalStateException("dispatch failure"))
                .when(dispatcher).dispatch(schedule);

        int processed = service.processDueSchedules();

        assertThat(processed).isZero();
        assertThat(schedule.getStatus()).isEqualTo(NotificationScheduleStatus.FAILED);
        assertThat(schedule.getProcessedAt()).isNull();
    }

    private Contract contract(ContractStatus status, LocalDate endDate) {
        return new Contract(
                "SCHEDULE-TEST", "PROC", "Objeto", "Empresa", null,
                TODAY, endDate, new BigDecimal("100.00"), status, null, "test");
    }
}

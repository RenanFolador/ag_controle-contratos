package com.organization.contractmanager.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.dto.ContractAssignmentCreateRequest;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.dto.ContractUpdateRequest;
import com.organization.contractmanager.dto.PersonCreateRequest;
import com.organization.contractmanager.repository.NotificationRepository;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
import com.organization.contractmanager.service.ContractAssignmentService;
import com.organization.contractmanager.service.ContractService;
import com.organization.contractmanager.service.NotificationScheduleService;
import com.organization.contractmanager.service.NotificationService;
import com.organization.contractmanager.service.PersonService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.mail.host=127.0.0.1",
        "spring.mail.port=1",
        "spring.mail.properties.mail.smtp.connectiontimeout=100",
        "spring.mail.properties.mail.smtp.timeout=100",
        "spring.mail.properties.mail.smtp.writetimeout=100"
})
@Testcontainers
@Transactional
class PostgreSqlContractWorkflowIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("contract_manager_integration")
            .withUsername("contract_manager")
            .withPassword("integration-test-only");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.database-platform",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired PersonService personService;
    @Autowired ContractService contractService;
    @Autowired ContractAssignmentService assignmentService;
    @Autowired NotificationScheduleService scheduleService;
    @Autowired NotificationService notificationService;
    @Autowired NotificationScheduleRepository scheduleRepository;
    @Autowired NotificationRepository notificationRepository;

    @Test
    void executesCompleteContractNotificationWorkflowAndPreventsDuplicates() {
        LocalDate today = LocalDate.now();
        LocalDate originalExpiration = today.plusDays(90);
        LocalDate newExpiration = today.plusDays(60);

        var person = createPerson("workflow");
        var contract = createContract("PG-WORKFLOW", originalExpiration);
        assignmentService.create(contract.id(), new ContractAssignmentCreateRequest(
                person.id(), ContractRole.PRIMARY_INSPECTOR, today, null));

        List<NotificationSchedule> originals = schedules(contract.id(), originalExpiration);
        assertThat(originals).hasSize(3)
                .allMatch(schedule -> schedule.getStatus() == NotificationScheduleStatus.PENDING);

        contractService.update(contract.id(), updateRequest(contract.contractNumber(), newExpiration));

        assertThat(schedules(contract.id(), originalExpiration)).hasSize(3)
                .allMatch(schedule -> schedule.getStatus() == NotificationScheduleStatus.CANCELLED);
        List<NotificationSchedule> replacements = schedules(contract.id(), newExpiration);
        assertThat(replacements).hasSize(3)
                .allMatch(schedule -> schedule.getExpirationDate().equals(newExpiration));

        assertThat(scheduleService.processDueSchedules()).isEqualTo(1);
        NotificationSchedule processed = replacements.stream()
                .filter(schedule -> schedule.getDaysBefore() == 60)
                .findFirst().orElseThrow();
        assertThat(processed.getStatus()).isEqualTo(NotificationScheduleStatus.PROCESSED);
        assertThat(notificationRepository.findAll()).filteredOn(notification ->
                notification.getContract().getId().equals(contract.id())).hasSize(1);

        assertThat(notificationService.createForSchedule(processed)).isEmpty();
        assertThat(notificationRepository.findAll()).filteredOn(notification ->
                notification.getContract().getId().equals(contract.id())).hasSize(1);
    }

    @Test
    void cancelsDueSchedulesAndCreatesNoNotificationsForClosedAndCancelledContracts() {
        LocalDate today = LocalDate.now();
        var person = createPerson("terminal-status");
        var closed = createContract("PG-CLOSED", today.plusDays(60));
        var cancelled = createContract("PG-CANCELLED", today.plusDays(60));
        assignmentService.create(closed.id(), assignment(person.id(), today));
        assignmentService.create(cancelled.id(), assignment(person.id(), today));

        contractService.close(closed.id());
        contractService.cancel(cancelled.id());

        assertThat(scheduleService.processDueSchedules()).isZero();
        assertThat(schedules(closed.id(), closed.endDate())).filteredOn(schedule ->
                !schedule.getScheduledDate().isAfter(today))
                .allMatch(schedule -> schedule.getStatus() == NotificationScheduleStatus.CANCELLED);
        assertThat(schedules(cancelled.id(), cancelled.endDate())).filteredOn(schedule ->
                !schedule.getScheduledDate().isAfter(today))
                .allMatch(schedule -> schedule.getStatus() == NotificationScheduleStatus.CANCELLED);
        assertThat(notificationRepository.findAll()).noneMatch(notification ->
                notification.getContract().getId().equals(closed.id())
                        || notification.getContract().getId().equals(cancelled.id()));
    }

    private com.organization.contractmanager.dto.PersonResponse createPerson(String suffix) {
        return personService.create(new PersonCreateRequest(
                "Fiscal " + suffix, null, "REG-" + suffix,
                suffix + "@example.com", null, false));
    }

    private com.organization.contractmanager.dto.ContractResponse createContract(
            String number, LocalDate expiration) {
        return contractService.create(new ContractCreateRequest(
                number, "PROC-" + number, "Objeto de teste", "Empresa teste", null,
                LocalDate.now(), expiration, new BigDecimal("1000.00"), null, null));
    }

    private ContractAssignmentCreateRequest assignment(UUID personId, LocalDate startDate) {
        return new ContractAssignmentCreateRequest(
                personId, ContractRole.PRIMARY_INSPECTOR, startDate, null);
    }

    private ContractUpdateRequest updateRequest(String number, LocalDate expiration) {
        return new ContractUpdateRequest(
                number, "PROC-" + number, "Objeto de teste", "Empresa teste", null,
                LocalDate.now(), expiration, new BigDecimal("1000.00"),
                ContractStatus.ACTIVE, null);
    }

    private List<NotificationSchedule> schedules(UUID contractId, LocalDate expiration) {
        return scheduleRepository.findAllByContractIdOrderByDaysBeforeDesc(contractId).stream()
                .filter(schedule -> schedule.getExpirationDate().equals(expiration))
                .toList();
    }
}

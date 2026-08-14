package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
import com.organization.contractmanager.repository.ContractHistoryRepository;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.dto.ContractRenewalRequest;
import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ContractScheduleIntegrationTests {

    @Autowired
    private ContractService contractService;

    @Autowired
    private NotificationScheduleRepository scheduleRepository;

    @Autowired
    private ContractHistoryRepository historyRepository;

    @Test
    void creatingActiveContractAutomaticallyPersistsEnabledSchedules() {
        LocalDate expirationDate = LocalDate.now().plusDays(120);

        var contract = contractService.create(new ContractCreateRequest(
                "AUTO-SCHEDULE-001", "PROC-AUTO", "Objeto", "Empresa", null,
                LocalDate.now(), expirationDate, new BigDecimal("100.00"), null, null));

        var schedules = scheduleRepository
                .findAllByContractIdOrderByDaysBeforeDesc(contract.id());
        assertThat(schedules).hasSize(3);
        assertThat(schedules).extracting(schedule -> schedule.getDaysBefore())
                .containsExactly(60, 30, 15);
        assertThat(schedules).allMatch(schedule ->
                schedule.getExpirationDate().equals(expirationDate)
                        && schedule.getScheduledDate().equals(
                                expirationDate.minusDays(schedule.getDaysBefore()))
                        && schedule.getStatus() == NotificationScheduleStatus.PENDING);
    }

    @Test
    void renewalCancelsOnlyPendingOldSchedulesAndCreatesDeadlineBasedFutureSchedules() {
        LocalDate previousExpiration = LocalDate.now().plusDays(120);
        var contract = contractService.create(new ContractCreateRequest(
                "RENEW-SCHEDULE-001", "PROC-RENEW", "Objeto", "Empresa", null,
                LocalDate.now(), previousExpiration, new BigDecimal("100.00"), null, null));
        var previousSchedules = scheduleRepository
                .findAllByContractIdOrderByDaysBeforeDesc(contract.id());
        var processed = previousSchedules.getFirst();
        processed.markProcessing();
        processed.markProcessed(Instant.now());
        scheduleRepository.saveAndFlush(processed);

        LocalDate newExpiration = previousExpiration.plusYears(1);
        var renewed = contractService.renew(contract.id(), new ContractRenewalRequest(
                newExpiration, "Prorrogação de vigência", "1º Termo Aditivo", "Mais 12 meses"));

        assertThat(renewed.endDate()).isEqualTo(newExpiration);
        var allSchedules = scheduleRepository.findAllByContractIdOrderByDaysBeforeDesc(contract.id());
        assertThat(allSchedules).hasSize(6);
        assertThat(allSchedules.stream().filter(item -> item.getExpirationDate().equals(previousExpiration)))
                .anyMatch(item -> item.getStatus() == NotificationScheduleStatus.PROCESSED)
                .allMatch(item -> item.getStatus() == NotificationScheduleStatus.PROCESSED
                        || item.getStatus() == NotificationScheduleStatus.CANCELLED);
        assertThat(allSchedules.stream().filter(item -> item.getExpirationDate().equals(newExpiration)))
                .hasSize(3)
                .extracting(item -> item.getDaysBefore()).containsExactlyInAnyOrder(60, 30, 15);
        assertThat(historyRepository.findAllByContractIdOrderByTimestampDesc(contract.id()))
                .anyMatch(item -> item.getAction() == HistoryAction.RENEW_CONTRACT
                        && item.getOldValue().contains(previousExpiration.toString())
                        && item.getNewValue().contains("1º Termo Aditivo"));
    }
}

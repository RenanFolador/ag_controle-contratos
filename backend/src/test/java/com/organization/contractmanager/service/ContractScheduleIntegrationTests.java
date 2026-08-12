package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
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
}

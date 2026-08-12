package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class NotificationScheduleRepositoryTests {

    @Autowired
    private NotificationScheduleRepository scheduleRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    void persistsPendingSchedule() {
        Contract contract = contractRepository.saveAndFlush(contract("SCHEDULE-001"));
        NotificationSchedule saved = scheduleRepository.saveAndFlush(
                new NotificationSchedule(
                        contract, contract.getEndDate(), 30,
                        contract.getEndDate().minusDays(30)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(NotificationScheduleStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(scheduleRepository.findAllByContractIdOrderByDaysBeforeDesc(contract.getId()))
                .hasSize(1);
    }

    @Test
    void rejectsDuplicateScheduleForSameExpirationAndDeadline() {
        Contract contract = contractRepository.saveAndFlush(contract("SCHEDULE-002"));
        LocalDate scheduledDate = contract.getEndDate().minusDays(30);
        scheduleRepository.saveAndFlush(
                new NotificationSchedule(contract, contract.getEndDate(), 30, scheduledDate));

        assertThatThrownBy(() -> scheduleRepository.saveAndFlush(
                new NotificationSchedule(contract, contract.getEndDate(), 30, scheduledDate)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsPendingSchedulesDueTodayOrEarlierAndExcludesFutureOnes() {
        Contract contract = contractRepository.saveAndFlush(contract("SCHEDULE-003"));
        LocalDate today = LocalDate.of(2026, 8, 12);
        NotificationSchedule overdue = scheduleRepository.saveAndFlush(
                new NotificationSchedule(contract, today.plusDays(10), 15, today.minusDays(5)));
        NotificationSchedule dueToday = scheduleRepository.saveAndFlush(
                new NotificationSchedule(contract, today.plusDays(30), 30, today));
        scheduleRepository.saveAndFlush(
                new NotificationSchedule(contract, today.plusDays(61), 60, today.plusDays(1)));
        Contract suspended = contractRepository.saveAndFlush(
                contract("SCHEDULE-SUSPENDED", ContractStatus.SUSPENDED));
        scheduleRepository.saveAndFlush(
                new NotificationSchedule(suspended, today.plusDays(15), 15, today));

        var due = scheduleRepository.findDueSchedules(
                NotificationScheduleStatus.PENDING, today,
                org.springframework.data.domain.PageRequest.of(0, 100));

        assertThat(due).extracting(NotificationSchedule::getId)
                .containsExactly(overdue.getId(), dueToday.getId());
    }

    private Contract contract(String number) {
        return contract(number, ContractStatus.ACTIVE);
    }

    private Contract contract(String number, ContractStatus status) {
        return new Contract(
                number, "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), status, null, "test");
    }
}

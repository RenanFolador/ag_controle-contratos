package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.repository.NotificationDeadlineRepository;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationScheduleService {

    private final NotificationDeadlineRepository deadlineRepository;
    private final NotificationScheduleRepository scheduleRepository;
    private final Clock clock;
    private final NotificationDispatcher dispatcher;

    @Autowired
    public NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            NotificationDispatcher dispatcher) {
        this(deadlineRepository, scheduleRepository,
                Clock.system(ZoneId.of("America/Sao_Paulo")), dispatcher);
    }

    NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            Clock clock) {
        this(deadlineRepository, scheduleRepository, clock, schedule -> { });
    }

    NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            Clock clock,
            NotificationDispatcher dispatcher) {
        this.deadlineRepository = deadlineRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
        this.dispatcher = dispatcher;
    }

    @Transactional
    public List<NotificationSchedule> createForActiveContract(Contract contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            return List.of();
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate expirationDate = contract.getEndDate();
        List<NotificationSchedule> schedules = deadlineRepository
                .findAllByEnabledTrueOrderByDaysBeforeDesc().stream()
                .map(deadline -> new NotificationSchedule(
                        contract, expirationDate, deadline.getDaysBefore(),
                        expirationDate.minusDays(deadline.getDaysBefore())))
                .filter(schedule -> !schedule.getScheduledDate().isBefore(today))
                .filter(schedule -> !scheduleRepository
                        .existsByContractIdAndExpirationDateAndDaysBefore(
                                contract.getId(), expirationDate, schedule.getDaysBefore()))
                .toList();

        return scheduleRepository.saveAll(schedules);
    }

    @Transactional
    public List<NotificationSchedule> rescheduleForExpirationChange(
            Contract contract, LocalDate previousExpirationDate) {
        List<NotificationSchedule> previousPending = scheduleRepository
                .findPendingByContractIdAndExpirationDate(
                        contract.getId(), previousExpirationDate);
        previousPending.forEach(NotificationSchedule::cancelIfPending);
        scheduleRepository.saveAll(previousPending);

        return createForActiveContract(contract);
    }

    @Transactional(readOnly = true)
    public List<NotificationSchedule> findByContract(Contract contract) {
        return scheduleRepository.findAllByContractIdOrderByDaysBeforeDesc(contract.getId());
    }

    @Transactional
    public int processDueSchedules() {
        LocalDate today = LocalDate.now(clock);
        List<NotificationSchedule> dueSchedules = scheduleRepository.findDueSchedules(
                NotificationScheduleStatus.PENDING, today);
        int processed = 0;

        for (NotificationSchedule schedule : dueSchedules) {
            ContractStatus contractStatus = schedule.getContract().getStatus();
            if (contractStatus == ContractStatus.CLOSED
                    || contractStatus == ContractStatus.CANCELLED) {
                schedule.cancelIfPending();
                continue;
            }
            if (contractStatus != ContractStatus.ACTIVE) {
                continue;
            }

            schedule.markProcessing();
            try {
                dispatcher.dispatch(schedule);
                schedule.markProcessed(Instant.now(clock));
                processed++;
            } catch (RuntimeException exception) {
                schedule.markFailed();
            }
        }
        scheduleRepository.saveAll(dueSchedules);
        return processed;
    }
}

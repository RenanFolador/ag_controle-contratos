package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import com.organization.contractmanager.repository.NotificationDeadlineRepository;
import com.organization.contractmanager.repository.NotificationScheduleRepository;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.domain.NotificationDeadline;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;

@Service
public class NotificationScheduleService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotificationScheduleService.class);

    private final NotificationDeadlineRepository deadlineRepository;
    private final NotificationScheduleRepository scheduleRepository;
    private final Clock clock;
    private final NotificationDispatcher dispatcher;
    private final ContractRepository contractRepository;
    private final int batchSize;

    @Autowired
    public NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            NotificationDispatcher dispatcher,
            ContractRepository contractRepository,
            @Value("${notification.batch-size:100}") int batchSize) {
        this(deadlineRepository, scheduleRepository,
                Clock.system(ZoneId.of("America/Sao_Paulo")), dispatcher,
                contractRepository, batchSize);
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
        this(deadlineRepository, scheduleRepository, clock, dispatcher, null, 100);
    }

    NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            Clock clock,
            NotificationDispatcher dispatcher,
            ContractRepository contractRepository) {
        this(deadlineRepository, scheduleRepository, clock, dispatcher,
                contractRepository, 100);
    }

    NotificationScheduleService(
            NotificationDeadlineRepository deadlineRepository,
            NotificationScheduleRepository scheduleRepository,
            Clock clock,
            NotificationDispatcher dispatcher,
            ContractRepository contractRepository,
            int batchSize) {
        this.deadlineRepository = deadlineRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
        this.dispatcher = dispatcher;
        this.contractRepository = contractRepository;
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException(
                    "notification.batch-size must be between 1 and 1000");
        }
        this.batchSize = batchSize;
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

    @Transactional
    public List<NotificationSchedule> createFutureForDeadline(NotificationDeadline deadline) {
        if (!deadline.isEnabled()) {
            return List.of();
        }
        LocalDate today = LocalDate.now(clock);
        List<NotificationSchedule> schedules = contractRepository
                .findAllByStatus(ContractStatus.ACTIVE).stream()
                .map(contract -> new NotificationSchedule(
                        contract, contract.getEndDate(), deadline.getDaysBefore(),
                        contract.getEndDate().minusDays(deadline.getDaysBefore())))
                .filter(schedule -> !schedule.getScheduledDate().isBefore(today))
                .filter(schedule -> !scheduleRepository
                        .existsByContractIdAndExpirationDateAndDaysBefore(
                                schedule.getContract().getId(), schedule.getExpirationDate(),
                                schedule.getDaysBefore()))
                .toList();
        return scheduleRepository.saveAll(schedules);
    }

    @Transactional
    public int cancelPendingForDeadline(int daysBefore) {
        List<NotificationSchedule> pending = scheduleRepository
                .findAllByDaysBeforeAndStatus(daysBefore, NotificationScheduleStatus.PENDING);
        pending.forEach(NotificationSchedule::cancelIfPending);
        scheduleRepository.saveAll(pending);
        return pending.size();
    }

    @Transactional(readOnly = true)
    public List<NotificationSchedule> findByContract(Contract contract) {
        return scheduleRepository.findAllByContractIdOrderByDaysBeforeDesc(contract.getId());
    }

    @Transactional
    public int processDueSchedules() {
        LocalDate today = LocalDate.now(clock);
        int processed = 0;
        int found = 0;
        List<NotificationSchedule> dueSchedules;

        do {
            dueSchedules = scheduleRepository.findDueSchedules(
                    NotificationScheduleStatus.PENDING, today,
                    PageRequest.of(0, batchSize));
            found += dueSchedules.size();

            for (NotificationSchedule schedule : dueSchedules) {
                ContractStatus contractStatus = schedule.getContract().getStatus();
                if (contractStatus == ContractStatus.CLOSED
                        || contractStatus == ContractStatus.CANCELLED) {
                    schedule.cancelIfPending();
                    LOGGER.info("Notification schedule cancelled scheduleId={} contractId={} status={}",
                            schedule.getId(), schedule.getContract().getId(), contractStatus);
                    continue;
                }

                schedule.markProcessing();
                try {
                    dispatcher.dispatch(schedule);
                    schedule.markProcessed(Instant.now(clock));
                    processed++;
                    LOGGER.info("Notification schedule processed scheduleId={} contractId={}",
                            schedule.getId(), schedule.getContract().getId());
                } catch (RuntimeException exception) {
                    schedule.markFailed();
                    LOGGER.warn("Notification schedule failed scheduleId={} contractId={} errorType={}",
                            schedule.getId(), schedule.getContract().getId(),
                            exception.getClass().getSimpleName());
                }
            }
            scheduleRepository.saveAll(dueSchedules);
            scheduleRepository.flush();
        } while (dueSchedules.size() == batchSize);

        LOGGER.info("Pending notification schedules found count={} processingDate={}",
                found, today);
        return processed;
    }
}

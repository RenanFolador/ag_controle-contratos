package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.NotificationDeadline;
import com.organization.contractmanager.exception.DuplicateNotificationDeadlineException;
import com.organization.contractmanager.exception.NotificationDeadlineNotFoundException;
import com.organization.contractmanager.repository.NotificationDeadlineRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeadlineService {

    private final NotificationDeadlineRepository repository;
    private final NotificationScheduleService scheduleService;

    public NotificationDeadlineService(
            NotificationDeadlineRepository repository,
            NotificationScheduleService scheduleService) {
        this.repository = repository;
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public List<NotificationDeadline> findAll() {
        return repository.findAllByOrderByDaysBeforeDesc();
    }

    @Transactional(readOnly = true)
    public List<NotificationDeadline> findEnabled() {
        return repository.findAllByEnabledTrueOrderByDaysBeforeDesc();
    }

    @Transactional
    public NotificationDeadline create(int daysBefore, boolean enabled) {
        validateDaysBefore(daysBefore);
        if (repository.existsByDaysBefore(daysBefore)) {
            throw new DuplicateNotificationDeadlineException(daysBefore);
        }
        try {
            NotificationDeadline saved = repository.saveAndFlush(
                    new NotificationDeadline(daysBefore, enabled));
            if (enabled) {
                scheduleService.createFutureForDeadline(saved);
            }
            return saved;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateNotificationDeadlineException(daysBefore);
        }
    }

    @Transactional
    public NotificationDeadline setEnabled(UUID id, boolean enabled) {
        NotificationDeadline deadline = repository.findById(id)
                .orElseThrow(() -> new NotificationDeadlineNotFoundException(id));
        if (deadline.isEnabled() == enabled) {
            return deadline;
        }
        deadline.setEnabled(enabled);
        NotificationDeadline saved = repository.save(deadline);
        if (enabled) {
            scheduleService.createFutureForDeadline(saved);
        } else {
            scheduleService.cancelPendingForDeadline(saved.getDaysBefore());
        }
        return saved;
    }

    @Transactional
    public void remove(UUID id) {
        NotificationDeadline deadline = repository.findById(id)
                .orElseThrow(() -> new NotificationDeadlineNotFoundException(id));
        scheduleService.cancelPendingForDeadline(deadline.getDaysBefore());
        repository.delete(deadline);
    }

    private void validateDaysBefore(int daysBefore) {
        if (daysBefore <= 0) {
            throw new IllegalArgumentException("Days before must be greater than zero");
        }
    }
}

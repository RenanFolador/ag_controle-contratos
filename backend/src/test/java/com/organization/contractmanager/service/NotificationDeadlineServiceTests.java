package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.organization.contractmanager.domain.NotificationDeadline;
import com.organization.contractmanager.exception.DuplicateNotificationDeadlineException;
import com.organization.contractmanager.exception.NotificationDeadlineNotFoundException;
import com.organization.contractmanager.repository.NotificationDeadlineRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeadlineServiceTests {

    @Mock
    private NotificationDeadlineRepository repository;
    @Mock
    private NotificationScheduleService scheduleService;

    private NotificationDeadlineService service;

    @BeforeEach
    void setUp() {
        service = new NotificationDeadlineService(repository, scheduleService);
    }

    @Test
    void obtainsEnabledPeriodsFromRepository() {
        when(repository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .thenReturn(List.of(new NotificationDeadline(45, true)));

        assertThat(service.findEnabled())
                .extracting(NotificationDeadline::getDaysBefore)
                .containsExactly(45);
    }

    @Test
    void createsValidPeriod() {
        when(repository.existsByDaysBefore(45)).thenReturn(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationDeadline deadline = service.create(45, true);
        assertThat(deadline.getDaysBefore()).isEqualTo(45);
        verify(scheduleService).createFutureForDeadline(deadline);
    }

    @Test
    void rejectsNonPositivePeriod() {
        assertThatThrownBy(() -> service.create(0, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicatePeriod() {
        when(repository.existsByDaysBefore(60)).thenReturn(true);

        assertThatThrownBy(() -> service.create(60, true))
                .isInstanceOf(DuplicateNotificationDeadlineException.class);
    }

    @Test
    void reportsMissingPeriodWhenChangingStatus() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setEnabled(id, false))
                .isInstanceOf(NotificationDeadlineNotFoundException.class);
    }

    @Test
    void disablingCancelsOnlyPendingSchedulesForDeadline() {
        UUID id = UUID.randomUUID();
        NotificationDeadline deadline = new NotificationDeadline(30, true);
        when(repository.findById(id)).thenReturn(Optional.of(deadline));
        when(repository.save(deadline)).thenReturn(deadline);

        service.setEnabled(id, false);

        verify(scheduleService).cancelPendingForDeadline(30);
        assertThat(deadline.isEnabled()).isFalse();
    }

    @Test
    void removingCancelsPendingSchedulesAndDeletesConfiguration() {
        UUID id = UUID.randomUUID();
        NotificationDeadline deadline = new NotificationDeadline(7, true);
        when(repository.findById(id)).thenReturn(Optional.of(deadline));

        service.remove(id);

        verify(scheduleService).cancelPendingForDeadline(7);
        verify(repository).delete(deadline);
    }
}

package com.organization.contractmanager.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.NotificationSchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreparedNotificationDispatcherTests {

    @Test
    void scheduleDispatchGeneratesPersistentNotificationsWithoutExternalSending() {
        NotificationService notificationService = org.mockito.Mockito.mock(
                NotificationService.class);
        PreparedNotificationDispatcher dispatcher =
                new PreparedNotificationDispatcher(notificationService);
        Contract contract = new Contract(
                "DISPATCH-001", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "test");
        NotificationSchedule schedule = new NotificationSchedule(
                contract, contract.getEndDate(), 30,
                contract.getEndDate().minusDays(30));
        when(notificationService.createAndSendForSchedule(schedule)).thenReturn(List.of());

        dispatcher.dispatch(schedule);

        verify(notificationService).createAndSendForSchedule(schedule);
    }
}

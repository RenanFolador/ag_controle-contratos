package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.NotificationStatus;
import com.organization.contractmanager.dto.DashboardContractCounts;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DashboardServiceTests {
    @Test
    void combinesAggregatedContractCountsAndFailedNotifications() {
        ContractRepository contracts = org.mockito.Mockito.mock(ContractRepository.class);
        NotificationRepository notifications = org.mockito.Mockito.mock(NotificationRepository.class);
        LocalDate today = LocalDate.of(2026, 8, 12);
        when(contracts.dashboardCounts(today, today.plusDays(15),
                today.plusDays(30), today.plusDays(60)))
                .thenReturn(new DashboardContractCounts(10L, 2L, 1L, 3L, 5L));
        when(notifications.countByStatus(NotificationStatus.FAILED)).thenReturn(4L);
        DashboardService service = new DashboardService(
                contracts, notifications,
                Clock.fixed(Instant.parse("2026-08-12T12:00:00Z"), ZoneOffset.UTC));

        var response = service.getDashboard();

        assertThat(response.activeContracts()).isEqualTo(10);
        assertThat(response.expiredContracts()).isEqualTo(2);
        assertThat(response.expiringIn15Days()).isEqualTo(1);
        assertThat(response.expiringIn30Days()).isEqualTo(3);
        assertThat(response.expiringIn60Days()).isEqualTo(5);
        assertThat(response.failedNotifications()).isEqualTo(4);
        verify(notifications).countByStatus(NotificationStatus.FAILED);
    }
}

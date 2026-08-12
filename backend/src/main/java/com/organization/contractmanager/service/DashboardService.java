package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.NotificationStatus;
import com.organization.contractmanager.dto.DashboardResponse;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ContractRepository contractRepository;
    private final NotificationRepository notificationRepository;
    private final Clock clock;

    @Autowired
    public DashboardService(ContractRepository contractRepository,
                            NotificationRepository notificationRepository) {
        this(contractRepository, notificationRepository,
                Clock.system(ZoneId.of("America/Sao_Paulo")));
    }

    DashboardService(ContractRepository contractRepository,
                     NotificationRepository notificationRepository, Clock clock) {
        this.contractRepository = contractRepository;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now(clock);
        var counts = contractRepository.dashboardCounts(
                today, today.plusDays(15), today.plusDays(30), today.plusDays(60));
        return new DashboardResponse(
                counts.activeContracts(), counts.expiredContracts(),
                counts.expiringIn15Days(), counts.expiringIn30Days(),
                counts.expiringIn60Days(),
                notificationRepository.countByStatus(NotificationStatus.FAILED));
    }
}

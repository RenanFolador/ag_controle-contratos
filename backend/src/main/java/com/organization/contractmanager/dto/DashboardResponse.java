package com.organization.contractmanager.dto;

public record DashboardResponse(
        long activeContracts,
        long expiredContracts,
        long expiringIn15Days,
        long expiringIn30Days,
        long expiringIn60Days,
        long failedNotifications) {
}

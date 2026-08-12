package com.organization.contractmanager.dto;

public record DashboardContractCounts(
        Long activeContracts,
        Long expiredContracts,
        Long expiringIn15Days,
        Long expiringIn30Days,
        Long expiringIn60Days) {
}

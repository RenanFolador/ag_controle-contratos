package com.organization.contractmanager.report;

import com.organization.contractmanager.domain.ContractStatus;
import java.time.LocalDate;
import java.util.UUID;

public record ReportFilter(
        Integer year,
        ContractStatus status,
        LocalDate startDate,
        LocalDate endDate,
        UUID personId,
        String company) {
}

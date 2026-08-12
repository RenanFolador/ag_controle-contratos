package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.ContractRole;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContractAssignmentResponse(
        UUID id,
        UUID contractId,
        AssignmentPersonResponse person,
        ContractRole role,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        Instant createdAt,
        String createdBy) {
}

package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.ContractRole;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ContractAssignmentUpdateRequest(
        @NotNull UUID personId,
        @NotNull ContractRole role,
        @NotNull LocalDate startDate,
        LocalDate endDate) {
}

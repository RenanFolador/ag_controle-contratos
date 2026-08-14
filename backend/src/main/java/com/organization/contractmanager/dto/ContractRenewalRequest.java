package com.organization.contractmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ContractRenewalRequest(
        @NotNull LocalDate newEndDate,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 255) String reference,
        @Size(max = 2000) String notes) {
}

package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.ContractStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractUpdateRequest(
        @NotBlank @Size(max = 100) String contractNumber,
        @Size(max = 100) String processNumber,
        @NotBlank String object,
        @NotBlank @Size(max = 255) String companyName,
        @Size(max = 18) String companyCnpj,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal initialValue,
        @NotNull ContractStatus status,
        String notes) {
}

package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.ContractStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        String contractNumber,
        String processNumber,
        String object,
        String companyName,
        String companyCnpj,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal initialValue,
        ContractStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}

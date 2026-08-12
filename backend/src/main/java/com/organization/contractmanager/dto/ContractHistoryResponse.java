package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.HistoryAction;
import java.time.Instant;
import java.util.UUID;

public record ContractHistoryResponse(
        UUID id, String actor, Instant timestamp, String entityType,
        UUID entityId, HistoryAction action, String oldValue, String newValue) { }

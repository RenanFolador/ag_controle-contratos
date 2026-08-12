package com.organization.contractmanager.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDeadlineResponse(
        UUID id, int daysBefore, boolean enabled, Instant createdAt, Instant updatedAt) { }

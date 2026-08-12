package com.organization.contractmanager.dto;

import jakarta.validation.constraints.Min;

public record NotificationDeadlineRequest(@Min(1) int daysBefore, boolean enabled) { }

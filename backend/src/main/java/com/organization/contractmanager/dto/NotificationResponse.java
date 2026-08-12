package com.organization.contractmanager.dto;

import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID contractId,
        String contractNumber,
        String companyName,
        UUID personId,
        String recipientName,
        String recipientAddress,
        NotificationChannel channel,
        int daysBefore,
        LocalDate expirationDate,
        LocalDate scheduledDate,
        NotificationStatus status,
        Instant sentAt,
        String errorMessage,
        int retryCount,
        Instant createdAt) { }

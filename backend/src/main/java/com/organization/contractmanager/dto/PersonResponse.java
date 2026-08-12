package com.organization.contractmanager.dto;

import java.time.Instant;
import java.util.UUID;

public record PersonResponse(
        UUID id,
        String name,
        String cpf,
        String registration,
        String email,
        String phone,
        boolean whatsappEnabled,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}

package com.organization.contractmanager.dto;

import java.util.UUID;

public record AssignmentPersonResponse(
        UUID id,
        String name,
        String registration,
        String email,
        String phone,
        boolean whatsappEnabled,
        boolean active) {
}

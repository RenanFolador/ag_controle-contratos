package com.organization.contractmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonUpdateRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 14) String cpf,
        @Size(max = 100) String registration,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        boolean whatsappEnabled,
        boolean active) {
}

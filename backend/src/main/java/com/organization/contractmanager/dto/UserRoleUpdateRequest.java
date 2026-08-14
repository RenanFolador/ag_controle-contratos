package com.organization.contractmanager.dto;

import com.organization.contractmanager.security.ApplicationRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserRoleUpdateRequest(
        @NotNull @Size(max = 4) Set<ApplicationRole> roles) {
}

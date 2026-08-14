package com.organization.contractmanager.dto;

import com.organization.contractmanager.security.ApplicationRole;
import java.util.Set;

public record UserResponse(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        Set<ApplicationRole> roles) {
}

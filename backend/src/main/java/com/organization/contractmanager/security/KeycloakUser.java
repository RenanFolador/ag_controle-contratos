package com.organization.contractmanager.security;

import java.util.Set;

public record KeycloakUser(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        Set<ApplicationRole> roles) {
}

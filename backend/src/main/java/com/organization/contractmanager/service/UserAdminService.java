package com.organization.contractmanager.service;

import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.UserResponse;
import com.organization.contractmanager.dto.UserRoleUpdateRequest;
import com.organization.contractmanager.exception.KeycloakAdminException;
import com.organization.contractmanager.security.ApplicationRole;
import com.organization.contractmanager.security.KeycloakAdminClient;
import com.organization.contractmanager.security.KeycloakUser;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {

    private final KeycloakAdminClient client;

    public UserAdminService(KeycloakAdminClient client) {
        this.client = client;
    }

    public PageResponse<UserResponse> findAll(int page, int size, String search) {
        validatePage(page, size);
        int first = Math.multiplyExact(page, size);
        var users = client.findUsers(search, first, size).stream()
                .map(this::toResponse)
                .toList();
        long total = client.countUsers(search);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageResponse<>(users, page, size, total, totalPages,
                page == 0, page + 1 >= totalPages);
    }

    public UserResponse updateRoles(String userId, UserRoleUpdateRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        Set<ApplicationRole> roles = request.roles();
        if (roles == null) {
            throw new IllegalArgumentException("Roles are required");
        }
        preventSelfLockout(userId, roles);
        return toResponse(client.replaceRealmRoles(userId, roles));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
    }

    private void preventSelfLockout(String userId, Set<ApplicationRole> roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && userId.equals(authentication.getName())
                && !roles.contains(ApplicationRole.ADMIN)) {
            throw new KeycloakAdminException(
                    "An administrator cannot remove the ADMIN role from the current user", 409);
        }
    }

    private UserResponse toResponse(KeycloakUser user) {
        return new UserResponse(user.id(), user.username(), user.firstName(), user.lastName(),
                user.email(), user.enabled(), user.roles());
    }
}

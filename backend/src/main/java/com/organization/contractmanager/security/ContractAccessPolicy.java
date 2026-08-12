package com.organization.contractmanager.security;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.organization.contractmanager.repository.ContractAssignmentRepository;

@Component
public class ContractAccessPolicy {
    private final ContractAssignmentRepository assignments;

    public ContractAccessPolicy(ContractAssignmentRepository assignments) {
        this.assignments = assignments;
    }

    public UUID restrictPersonFilter(UUID requestedPersonId) {
        if (!isInspector()) {
            return requestedPersonId;
        }
        return inspectorPersonId();
    }

    public void checkContract(UUID contractId) {
        if (isInspector() && !assignments.existsByContractIdAndPersonId(
                contractId, inspectorPersonId())) {
            throw new AccessDeniedException("Inspector is not assigned to this contract");
        }
    }

    private boolean isInspector() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean inspector = hasRole(authentication, "ROLE_INSPECTOR");
        boolean unrestricted = hasRole(authentication, "ROLE_ADMIN")
                || hasRole(authentication, "ROLE_CONTRACT_MANAGER")
                || hasRole(authentication, "ROLE_VIEWER");
        return inspector && !unrestricted;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private UUID inspectorPersonId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new AccessDeniedException("Inspector identity is not a JWT");
        }
        String personId = jwtAuthentication.getToken().getClaimAsString("person_id");
        try {
            return UUID.fromString(personId);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new AccessDeniedException("JWT does not contain a valid person_id claim");
        }
    }
}

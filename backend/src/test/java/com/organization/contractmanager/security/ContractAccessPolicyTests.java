package com.organization.contractmanager.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.repository.ContractAssignmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class ContractAccessPolicyTests {
    private final ContractAssignmentRepository repository =
            mock(ContractAssignmentRepository.class);
    private final ContractAccessPolicy policy = new ContractAccessPolicy(repository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forcesInspectorPersonIdAndChecksAssignment() {
        UUID personId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        authenticateInspector(Map.of("person_id", personId.toString()));
        when(repository.existsByContractIdAndPersonId(contractId, personId)).thenReturn(true);

        assertThat(policy.restrictPersonFilter(UUID.randomUUID())).isEqualTo(personId);
        policy.checkContract(contractId);

        verify(repository).existsByContractIdAndPersonId(contractId, personId);
    }

    @Test
    void deniesInspectorWithoutValidPersonClaim() {
        authenticateInspector(Map.of());

        assertThatThrownBy(() -> policy.restrictPersonFilter(null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void doesNotRestrictNonInspector() {
        UUID requested = UUID.randomUUID();
        authenticate("ROLE_VIEWER", Map.of());

        assertThat(policy.restrictPersonFilter(requested)).isEqualTo(requested);
    }

    private void authenticateInspector(Map<String, Object> claims) {
        authenticate("ROLE_INSPECTOR", claims);
    }

    private void authenticate(String authority, Map<String, Object> additionalClaims) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claims(claims -> claims.putAll(additionalClaims))
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

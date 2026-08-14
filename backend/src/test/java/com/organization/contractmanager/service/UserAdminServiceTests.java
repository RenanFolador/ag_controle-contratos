package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.dto.UserRoleUpdateRequest;
import com.organization.contractmanager.exception.KeycloakAdminException;
import com.organization.contractmanager.security.ApplicationRole;
import com.organization.contractmanager.security.KeycloakAdminClient;
import com.organization.contractmanager.security.KeycloakUser;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTests {

    @Mock
    private KeycloakAdminClient client;

    @InjectMocks
    private UserAdminService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsUsersWithRolesAndPagination() {
        KeycloakUser user = user("user-1", Set.of(ApplicationRole.VIEWER));
        when(client.findUsers("maria", 20, 20)).thenReturn(List.of(user));
        when(client.countUsers("maria")).thenReturn(41L);

        var result = service.findAll(1, 20, "maria");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().roles()).containsExactly(ApplicationRole.VIEWER);
        assertThat(result.totalElements()).isEqualTo(41);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.first()).isFalse();
        assertThat(result.last()).isFalse();
        verify(client).findUsers("maria", 20, 20);
    }

    @Test
    void updatesRolesThroughKeycloakClient() {
        authenticateAs("admin-user");
        KeycloakUser updated = user("user-1", Set.of(ApplicationRole.CONTRACT_MANAGER));
        when(client.replaceRealmRoles("user-1", Set.of(ApplicationRole.CONTRACT_MANAGER)))
                .thenReturn(updated);

        var result = service.updateRoles("user-1",
                new UserRoleUpdateRequest(Set.of(ApplicationRole.CONTRACT_MANAGER)));

        assertThat(result.roles()).containsExactly(ApplicationRole.CONTRACT_MANAGER);
        verify(client).replaceRealmRoles("user-1", Set.of(ApplicationRole.CONTRACT_MANAGER));
    }

    @Test
    void preventsAdministratorFromRemovingOwnAdminRole() {
        authenticateAs("admin-user");

        assertThatThrownBy(() -> service.updateRoles("admin-user",
                new UserRoleUpdateRequest(Set.of(ApplicationRole.VIEWER))))
                .isInstanceOf(KeycloakAdminException.class)
                .hasMessageContaining("remove the ADMIN role");
    }

    private void authenticateAs(String subject) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private KeycloakUser user(String id, Set<ApplicationRole> roles) {
        return new KeycloakUser(id, "maria", "Maria", "Silva",
                "maria@example.com", true, roles);
    }
}

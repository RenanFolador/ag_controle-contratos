package com.organization.contractmanager.security;

import com.organization.contractmanager.exception.KeycloakAdminException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KeycloakAdminRestClient implements KeycloakAdminClient {

    private static final ParameterizedTypeReference<List<UserRepresentation>> USERS_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<RoleRepresentation>> ROLES_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final boolean enabled;
    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final Object tokenMonitor = new Object();
    private volatile AccessToken accessToken;

    public KeycloakAdminRestClient(
            RestClient.Builder builder,
            @Value("${keycloak.admin.enabled:false}") boolean enabled,
            @Value("${keycloak.admin.base-url:}") String baseUrl,
            @Value("${keycloak.admin.realm:${KEYCLOAK_REALM:contract-manager}}") String realm,
            @Value("${keycloak.admin.client-id:}") String clientId,
            @Value("${keycloak.admin.client-secret:}") String clientSecret) {
        this.enabled = enabled;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = builder.baseUrl(this.baseUrl).build();
    }

    @Override
    public List<KeycloakUser> findUsers(String search, int first, int max) {
        List<UserRepresentation> representations = call(() -> restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/admin/realms/{realm}/users")
                            .queryParam("first", first)
                            .queryParam("max", max);
                    if (hasText(search)) {
                        builder.queryParam("search", search.trim());
                    }
                    return builder.build(realm);
                })
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .retrieve()
                .body(USERS_TYPE));
        if (representations == null) {
            return List.of();
        }
        return representations.stream().map(this::withRoles).toList();
    }

    @Override
    public long countUsers(String search) {
        Long count = call(() -> restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/admin/realms/{realm}/users/count");
                    if (hasText(search)) {
                        builder.queryParam("search", search.trim());
                    }
                    return builder.build(realm);
                })
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .retrieve()
                .body(Long.class));
        return count == null ? 0 : count;
    }

    @Override
    public KeycloakUser replaceRealmRoles(String userId, Set<ApplicationRole> roles) {
        List<RoleRepresentation> current = roleMappings(userId);
        Map<String, RoleRepresentation> currentByName = new HashMap<>();
        current.forEach(role -> currentByName.put(role.name(), role));

        List<RoleRepresentation> additions = new ArrayList<>();
        for (ApplicationRole role : roles) {
            if (!currentByName.containsKey(role.name())) {
                additions.add(roleRepresentation(role));
            }
        }

        EnumSet<ApplicationRole> desired = roles.isEmpty()
                ? EnumSet.noneOf(ApplicationRole.class)
                : EnumSet.copyOf(roles);
        List<RoleRepresentation> removals = current.stream()
                .filter(role -> isApplicationRole(role.name()))
                .filter(role -> !desired.contains(ApplicationRole.valueOf(role.name())))
                .toList();

        if (!additions.isEmpty()) {
            call(() -> restClient.post()
                    .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(additions)
                    .retrieve()
                    .toBodilessEntity());
        }
        if (!removals.isEmpty()) {
            call(() -> restClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(removals)
                    .retrieve()
                    .toBodilessEntity());
        }
        return withRoles(user(userId));
    }

    private UserRepresentation user(String userId) {
        return call(() -> restClient.get()
                .uri("/admin/realms/{realm}/users/{userId}", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .retrieve()
                .body(UserRepresentation.class));
    }

    private KeycloakUser withRoles(UserRepresentation user) {
        Set<ApplicationRole> roles = roleMappings(user.id()).stream()
                .map(RoleRepresentation::name)
                .filter(this::isApplicationRole)
                .map(ApplicationRole::valueOf)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new KeycloakUser(user.id(), user.username(), user.firstName(), user.lastName(),
                user.email(), user.enabled(), roles);
    }

    private List<RoleRepresentation> roleMappings(String userId) {
        List<RoleRepresentation> roles = call(() -> restClient.get()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .retrieve()
                .body(ROLES_TYPE));
        return roles == null ? List.of() : roles;
    }

    private RoleRepresentation roleRepresentation(ApplicationRole role) {
        return call(() -> restClient.get()
                .uri("/admin/realms/{realm}/roles/{roleName}", realm, role.name())
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .retrieve()
                .body(RoleRepresentation.class));
    }

    private String bearerToken() {
        return "Bearer " + token();
    }

    private String token() {
        ensureConfigured();
        AccessToken current = accessToken;
        if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
            return current.value();
        }
        synchronized (tokenMonitor) {
            current = accessToken;
            if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
                return current.value();
            }
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            TokenResponse response = call(() -> restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class));
            if (response == null || !hasText(response.accessToken())) {
                throw new KeycloakAdminException("Keycloak admin token was not returned", 503);
            }
            accessToken = new AccessToken(response.accessToken(),
                    Instant.now().plusSeconds(Math.max(response.expiresIn(), 60)));
            return accessToken.value();
        }
    }

    private void ensureConfigured() {
        if (!enabled) {
            throw new KeycloakAdminException("Keycloak user administration is disabled", 503);
        }
        if (!hasText(baseUrl) || !hasText(realm) || !hasText(clientId)
                || !hasText(clientSecret)) {
            throw new KeycloakAdminException(
                    "Keycloak user administration is not configured", 503);
        }
    }

    private <T> T call(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            int mappedStatus = status == 401 || status == 403 ? 502 : 503;
            throw new KeycloakAdminException(
                    "Keycloak user administration request failed", mappedStatus, exception);
        } catch (RestClientException exception) {
            throw new KeycloakAdminException(
                    "Keycloak user administration is unavailable", 503, exception);
        }
    }

    private boolean isApplicationRole(String role) {
        try {
            ApplicationRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeBaseUrl(String value) {
        if (!hasText(value)) {
            return "http://localhost:8081";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record UserRepresentation(
            String id,
            String username,
            String firstName,
            String lastName,
            String email,
            boolean enabled) {
    }

    private record RoleRepresentation(
            String id,
            String name,
            boolean composite,
            boolean clientRole,
            String containerId) {
    }

    private record TokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn) {
    }

    private record AccessToken(String value, Instant expiresAt) {
    }
}

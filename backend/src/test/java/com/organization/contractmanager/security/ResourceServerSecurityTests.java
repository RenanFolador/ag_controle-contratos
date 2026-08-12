package com.organization.contractmanager.security;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.controller.DashboardController;
import com.organization.contractmanager.dto.DashboardResponse;
import com.organization.contractmanager.service.DashboardService;
import com.organization.contractmanager.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = DashboardController.class,
        properties = "application.cors.allowed-origins=http://localhost:4200")
@Import({SecurityConfig.class, CorsConfig.class})
class ResourceServerSecurityTests {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private DashboardService dashboardService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void rejectsProtectedApiWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.message").value("Sua sessão expirou ou não é válida."));
    }

    @Test
    void allowsConfiguredCorsPreflightWithoutCredentials() throws Exception {
        mockMvc.perform(options("/api/v1/dashboard")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        "http://localhost:4200"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void rejectsUnconfiguredCorsOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/dashboard")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsJwtAuthenticatedByResourceServer() throws Exception {
        when(dashboardService.getDashboard())
                .thenReturn(new DashboardResponse(0, 0, 0, 0, 0, 0));

        mockMvc.perform(get("/api/v1/dashboard").with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))
                        .jwt(token -> token.issuer("http://keycloak/realms/contracts")
                                .subject("viewer")
                                .claim("realm_access", java.util.Map.of(
                                        "roles", java.util.List.of("VIEWER"))))))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInspectorFromGlobalDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard").with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_INSPECTOR"))
                        .jwt(token -> token
                        .claim("realm_access", java.util.Map.of(
                                "roles", java.util.List.of("INSPECTOR"))))))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotCreateContracts() throws Exception {
        mockMvc.perform(post("/api/v1/contracts").with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))
                        .jwt(token -> token
                        .claim("realm_access", java.util.Map.of(
                                "roles", java.util.List.of("VIEWER"))))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid signature"));

        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}

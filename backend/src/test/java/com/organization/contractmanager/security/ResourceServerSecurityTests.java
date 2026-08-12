package com.organization.contractmanager.security;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.controller.DashboardController;
import com.organization.contractmanager.dto.DashboardResponse;
import com.organization.contractmanager.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class ResourceServerSecurityTests {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private DashboardService dashboardService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void rejectsProtectedApiWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsJwtAuthenticatedByResourceServer() throws Exception {
        when(dashboardService.getDashboard())
                .thenReturn(new DashboardResponse(0, 0, 0, 0, 0, 0));

        mockMvc.perform(get("/api/v1/dashboard").with(jwt()
                        .jwt(token -> token.issuer("http://keycloak/realms/contracts")
                                .subject("manager"))))
                .andExpect(status().isOk());
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

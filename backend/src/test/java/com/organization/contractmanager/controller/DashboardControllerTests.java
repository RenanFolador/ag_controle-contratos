package com.organization.contractmanager.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.dto.DashboardResponse;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private DashboardService service;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void returnsDashboardMetrics() throws Exception {
        when(service.getDashboard()).thenReturn(new DashboardResponse(10, 2, 1, 3, 5, 4));
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeContracts").value(10))
                .andExpect(jsonPath("$.expiredContracts").value(2))
                .andExpect(jsonPath("$.expiringIn15Days").value(1))
                .andExpect(jsonPath("$.expiringIn30Days").value(3))
                .andExpect(jsonPath("$.expiringIn60Days").value(5))
                .andExpect(jsonPath("$.failedNotifications").value(4));
    }
}

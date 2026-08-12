package com.organization.contractmanager.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.NotificationService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private NotificationService service;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delegatesNotificationFilters() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                .param("page", "1").param("size", "10")
                .param("status", "FAILED").param("channel", "EMAIL")
                .param("contract", "025/2026").param("date", "2026-08-12"))
                .andExpect(status().isOk());
        verify(service).findAll(1, 10, NotificationStatus.FAILED,
                NotificationChannel.EMAIL, "025/2026", LocalDate.of(2026, 8, 12));
    }
}

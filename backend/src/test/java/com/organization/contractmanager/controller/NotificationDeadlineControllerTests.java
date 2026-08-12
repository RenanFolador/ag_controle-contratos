package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.domain.NotificationDeadline;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.NotificationDeadlineService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationDeadlineController.class)
@Import(SecurityConfig.class)
class NotificationDeadlineControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private NotificationDeadlineService service;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notification-deadlines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CONTRACT_MANAGER")
    void rejectsNonAdministrator() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notification-deadlines"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listsDeadlinesForAdministrator() throws Exception {
        when(service.findAll()).thenReturn(List.of(new NotificationDeadline(90, true)));

        mockMvc.perform(get("/api/v1/admin/notification-deadlines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].daysBefore").value(90))
                .andExpect(jsonPath("$[0].enabled").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validatesAndCreatesDeadline() throws Exception {
        when(service.create(anyInt(), anyBoolean()))
                .thenReturn(new NotificationDeadline(7, true));

        mockMvc.perform(post("/api/v1/admin/notification-deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"daysBefore\":7,\"enabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.daysBefore").value(7));

        mockMvc.perform(post("/api/v1/admin/notification-deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"daysBefore\":0,\"enabled\":true}"))
                .andExpect(status().isBadRequest());
    }
}

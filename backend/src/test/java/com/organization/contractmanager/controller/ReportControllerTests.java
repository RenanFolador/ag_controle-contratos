package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.report.ReportFile;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReportService service;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/reports/export?type=ACTIVE_CONTRACTS"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void downloadsCsvForAuthorizedReader() throws Exception {
        when(service.export(any(), any(), any())).thenReturn(
                new ReportFile("csv".getBytes(), "text/csv;charset=UTF-8", "report.csv"));

        mockMvc.perform(get("/api/v1/reports/export?type=ACTIVE_CONTRACTS"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.csv\""))
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }
}

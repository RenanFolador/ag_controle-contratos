package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.dto.AssignmentPersonResponse;
import com.organization.contractmanager.dto.ContractAssignmentResponse;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.ContractAssignmentService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContractAssignmentController.class)
@Import(SecurityConfig.class)
class ContractAssignmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractAssignmentService service;

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/contracts/{id}/assignments", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void listsAssignmentsWithPersonData() throws Exception {
        UUID contractId = UUID.randomUUID();
        when(service.findByContract(contractId)).thenReturn(List.of(response(contractId, true)));

        mockMvc.perform(get("/api/v1/contracts/{id}/assignments", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].person.name").value("Maria da Silva"))
                .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    @Test
    @WithMockUser(username = "manager")
    void createsAssignment() throws Exception {
        UUID contractId = UUID.randomUUID();
        ContractAssignmentResponse response = response(contractId, true);
        when(service.create(eq(contractId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contracts/{id}/assignments", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/contracts/" + contractId
                        + "/assignments/" + response.id()))
                .andExpect(jsonPath("$.person.email").value("maria@example.com"));
    }

    @Test
    @WithMockUser
    void validatesAssignmentPayload() throws Exception {
        mockMvc.perform(post("/api/v1/contracts/{id}/assignments", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.personId").exists())
                .andExpect(jsonPath("$.fieldErrors.role").exists())
                .andExpect(jsonPath("$.fieldErrors.startDate").exists());
    }

    @Test
    @WithMockUser
    void updatesAssignment() throws Exception {
        UUID contractId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        when(service.update(eq(contractId), eq(assignmentId), any()))
                .thenReturn(response(contractId, true));

        mockMvc.perform(put("/api/v1/contracts/{contractId}/assignments/{assignmentId}",
                        contractId, assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void logicallyRemovesAssignment() throws Exception {
        UUID contractId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        LocalDate endDate = LocalDate.of(2026, 6, 30);
        when(service.remove(contractId, assignmentId, endDate))
                .thenReturn(response(contractId, false));

        mockMvc.perform(delete("/api/v1/contracts/{contractId}/assignments/{assignmentId}",
                        contractId, assignmentId).param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        verify(service).remove(contractId, assignmentId, endDate);
    }

    @Test
    @WithMockUser
    void returnsNotFoundWhenContractDoesNotExist() throws Exception {
        UUID contractId = UUID.randomUUID();
        when(service.findByContract(contractId)).thenThrow(new ContractNotFoundException(contractId));

        mockMvc.perform(get("/api/v1/contracts/{id}/assignments", contractId))
                .andExpect(status().isNotFound());
    }

    private String payload() {
        return """
                {"personId":"%s","role":"MANAGER","startDate":"2026-01-01"}
                """.formatted(UUID.randomUUID());
    }

    private ContractAssignmentResponse response(UUID contractId, boolean active) {
        return new ContractAssignmentResponse(
                UUID.randomUUID(), contractId,
                new AssignmentPersonResponse(
                        UUID.randomUUID(), "Maria da Silva", "MAT-1", "maria@example.com",
                        "11999990000", true, true),
                ContractRole.MANAGER, LocalDate.of(2026, 1, 1),
                active ? null : LocalDate.of(2026, 6, 30), active, Instant.now(), "manager");
    }
}

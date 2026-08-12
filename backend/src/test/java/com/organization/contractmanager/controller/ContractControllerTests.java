package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.dto.ContractResponse;
import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.ContractHistoryResponse;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.exception.DuplicateContractNumberException;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.ContractService;
import com.organization.contractmanager.security.ContractAccessPolicy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContractController.class)
@Import(SecurityConfig.class)
class ContractControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractService service;
    @MockitoBean
    private ContractAccessPolicy accessPolicy;

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/contracts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsContract() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(any())).thenReturn(response(id, ContractStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/contracts/" + id))
                .andExpect(jsonPath("$.contractNumber").value("025/2026"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validatesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.contractNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.object").exists())
                .andExpect(jsonPath("$.fieldErrors.companyName").exists())
                .andExpect(jsonPath("$.fieldErrors.startDate").exists())
                .andExpect(jsonPath("$.fieldErrors.endDate").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new ContractNotFoundException(id));

        mockMvc.perform(get("/api/v1/contracts/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnsConflictForDuplicateNumber() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateContractNumberException("025/2026"));

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closesContractByDelegatingToService() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.close(id)).thenReturn(response(id, ContractStatus.CLOSED));

        mockMvc.perform(post("/api/v1/contracts/{id}/close", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
        verify(service).close(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnsContractHistory() throws Exception {
        UUID contractId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();
        when(service.findHistory(contractId)).thenReturn(List.of(
                new ContractHistoryResponse(
                        historyId, "manager", Instant.parse("2026-08-12T12:00:00Z"),
                        "CONTRACT", contractId, HistoryAction.CREATE_CONTRACT,
                        null, "status=ACTIVE")));

        mockMvc.perform(get("/api/v1/contracts/{id}/history", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE_CONTRACT"))
                .andExpect(jsonPath("$[0].actor").value("manager"))
                .andExpect(jsonPath("$[0].entityId").value(contractId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delegatesPaginationAndFilters() throws Exception {
        UUID personId = UUID.randomUUID();
        when(accessPolicy.restrictPersonFilter(personId)).thenReturn(personId);
        when(service.findAll(
                1, 10, "endDate,desc", "empresa", ContractStatus.ACTIVE,
                2026, personId, 30))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false, true));

        mockMvc.perform(get("/api/v1/contracts")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "endDate,desc")
                        .param("search", "empresa")
                        .param("status", "ACTIVE")
                        .param("year", "2026")
                        .param("personId", personId.toString())
                        .param("expirationDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/contracts").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    private String validPayload() {
        return """
                {
                  "contractNumber": "025/2026",
                  "processNumber": "PROCESS-1",
                  "object": "Objeto",
                  "companyName": "Empresa",
                  "startDate": "2026-01-01",
                  "endDate": "2026-12-31",
                  "initialValue": 100.00
                }
                """;
    }

    private ContractResponse response(UUID id, ContractStatus status) {
        return new ContractResponse(
                id, "025/2026", "PROCESS-1", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), status, null, Instant.now(), Instant.now(),
                "creator", "editor");
    }
}

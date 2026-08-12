package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.dto.PersonResponse;
import com.organization.contractmanager.exception.DuplicatePersonCpfException;
import com.organization.contractmanager.exception.PersonNotFoundException;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.PersonService;
import java.time.Instant;
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

@WebMvcTest(PersonController.class)
@Import(SecurityConfig.class)
class PersonControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService service;

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/persons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createsPerson() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(any())).thenReturn(response(id, true));

        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/persons/" + id))
                .andExpect(jsonPath("$.name").value("Maria da Silva"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser
    void validatesNameAndEmail() throws Exception {
        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @WithMockUser
    void searchesByName() throws Exception {
        when(service.findAll("maria")).thenReturn(List.of(response(UUID.randomUUID(), true)));

        mockMvc.perform(get("/api/v1/persons").param("name", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maria da Silva"));
        verify(service).findAll("maria");
    }

    @Test
    @WithMockUser
    void returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new PersonNotFoundException(id));

        mockMvc.perform(get("/api/v1/persons/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void returnsConflictForDuplicateCpf() throws Exception {
        when(service.create(any())).thenThrow(new DuplicatePersonCpfException("123.456.789-01"));

        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void updatesAndDeactivatesPerson() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(any(), any())).thenReturn(response(id, false));

        mockMvc.perform(put("/api/v1/persons/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private String createPayload() {
        return """
                {"name":"Maria da Silva","cpf":"123.456.789-01",
                 "email":"maria@example.com","whatsappEnabled":true}
                """;
    }

    private String updatePayload() {
        return """
                {"name":"Maria da Silva","cpf":"123.456.789-01",
                 "email":"maria@example.com","whatsappEnabled":false,"active":false}
                """;
    }

    private PersonResponse response(UUID id, boolean active) {
        return new PersonResponse(
                id, "Maria da Silva", "123.456.789-01", "MAT-1",
                "maria@example.com", null, true, active, Instant.now(), Instant.now());
    }
}

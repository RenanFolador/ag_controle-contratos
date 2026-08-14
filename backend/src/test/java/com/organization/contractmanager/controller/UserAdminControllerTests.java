package com.organization.contractmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.UserResponse;
import com.organization.contractmanager.dto.UserRoleUpdateRequest;
import com.organization.contractmanager.security.ApplicationRole;
import com.organization.contractmanager.security.SecurityConfig;
import com.organization.contractmanager.service.UserAdminService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAdminController.class)
@Import(SecurityConfig.class)
class UserAdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAdminService service;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void rejectsNonAdministrator() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listsUsersForAdministrator() throws Exception {
        when(service.findAll(0, 20, "maria")).thenReturn(new PageResponse<>(
                List.of(user("user-1", Set.of(ApplicationRole.VIEWER))),
                0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("search", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("maria"))
                .andExpect(jsonPath("$.content[0].roles[0]").value("VIEWER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatesRolesForAdministrator() throws Exception {
        var id = "user-1";
        when(service.updateRoles(eq(id), any(UserRoleUpdateRequest.class)))
                .thenReturn(user(id, Set.of(ApplicationRole.INSPECTOR)));

        mockMvc.perform(put("/api/v1/admin/users/{userId}/roles", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"INSPECTOR\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("INSPECTOR"));

        verify(service).updateRoles(eq(id), any(UserRoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsUnknownRole() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/user-1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"ROOT\"]}"))
                .andExpect(status().isBadRequest());
    }

    private UserResponse user(String id, Set<ApplicationRole> roles) {
        return new UserResponse(id, "maria", "Maria", "Silva",
                "maria@example.com", true, roles);
    }
}

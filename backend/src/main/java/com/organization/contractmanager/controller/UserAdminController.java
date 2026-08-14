package com.organization.contractmanager.controller;

import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.UserResponse;
import com.organization.contractmanager.dto.UserRoleUpdateRequest;
import com.organization.contractmanager.service.UserAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<UserResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(max = 255) String search) {
        return service.findAll(page, size, search);
    }

    @PutMapping("/{userId}/roles")
    public UserResponse updateRoles(
            @PathVariable String userId,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        return service.updateRoles(userId, request);
    }
}

package com.organization.contractmanager.controller;

import com.organization.contractmanager.dto.ContractAssignmentCreateRequest;
import com.organization.contractmanager.dto.ContractAssignmentResponse;
import com.organization.contractmanager.dto.ContractAssignmentUpdateRequest;
import com.organization.contractmanager.service.ContractAssignmentService;
import com.organization.contractmanager.security.ContractAccessPolicy;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contracts/{contractId}/assignments")
public class ContractAssignmentController {

    private final ContractAssignmentService service;
    private final ContractAccessPolicy accessPolicy;

    public ContractAssignmentController(
            ContractAssignmentService service, ContractAccessPolicy accessPolicy) {
        this.service = service;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    public List<ContractAssignmentResponse> findAll(@PathVariable UUID contractId) {
        accessPolicy.checkContract(contractId);
        return service.findByContract(contractId);
    }

    @PostMapping
    public ResponseEntity<ContractAssignmentResponse> create(
            @PathVariable UUID contractId,
            @Valid @RequestBody ContractAssignmentCreateRequest request) {
        ContractAssignmentResponse response = service.create(contractId, request);
        return ResponseEntity.created(URI.create(
                "/api/v1/contracts/" + contractId + "/assignments/" + response.id()))
                .body(response);
    }

    @PutMapping("/{assignmentId}")
    public ContractAssignmentResponse update(
            @PathVariable UUID contractId,
            @PathVariable UUID assignmentId,
            @Valid @RequestBody ContractAssignmentUpdateRequest request) {
        return service.update(contractId, assignmentId, request);
    }

    @DeleteMapping("/{assignmentId}")
    public ContractAssignmentResponse remove(
            @PathVariable UUID contractId,
            @PathVariable UUID assignmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.remove(contractId, assignmentId, endDate);
    }
}

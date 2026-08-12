package com.organization.contractmanager.controller;

import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.dto.ContractResponse;
import com.organization.contractmanager.dto.ContractSummaryResponse;
import com.organization.contractmanager.dto.ContractUpdateRequest;
import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.ContractHistoryResponse;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.service.ContractService;
import com.organization.contractmanager.security.ContractAccessPolicy;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/contracts")
@Validated
public class ContractController {

    private final ContractService service;
    private final ContractAccessPolicy accessPolicy;

    public ContractController(ContractService service, ContractAccessPolicy accessPolicy) {
        this.service = service;
        this.accessPolicy = accessPolicy;
    }

    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractCreateRequest request) {
        ContractResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/contracts/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<ContractSummaryResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "contractNumber,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @Min(1900) @Max(9999) Integer year,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) @Min(0) Integer expirationDays) {
        return service.findAll(
                page, size, sort, search, status, year,
                accessPolicy.restrictPersonFilter(personId), expirationDays);
    }

    @GetMapping("/{id}")
    public ContractResponse findById(@PathVariable UUID id) {
        accessPolicy.checkContract(id);
        return service.findById(id);
    }

    @GetMapping("/{id}/history")
    public List<ContractHistoryResponse> history(@PathVariable UUID id) {
        accessPolicy.checkContract(id);
        return service.findHistory(id);
    }

    @PutMapping("/{id}")
    public ContractResponse update(
            @PathVariable UUID id, @Valid @RequestBody ContractUpdateRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/close")
    public ContractResponse close(@PathVariable UUID id) {
        return service.close(id);
    }

    @PostMapping("/{id}/cancel")
    public ContractResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}

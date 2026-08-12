package com.organization.contractmanager.controller;

import com.organization.contractmanager.dto.PersonCreateRequest;
import com.organization.contractmanager.dto.PersonResponse;
import com.organization.contractmanager.dto.PersonUpdateRequest;
import com.organization.contractmanager.service.PersonService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/persons")
@Validated
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest request) {
        PersonResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/persons/" + response.id())).body(response);
    }

    @GetMapping
    public List<PersonResponse> findAll(
            @RequestParam(required = false) @Size(max = 255) String name) {
        return service.findAll(name);
    }

    @GetMapping("/{id}")
    public PersonResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public PersonResponse update(
            @PathVariable UUID id, @Valid @RequestBody PersonUpdateRequest request) {
        return service.update(id, request);
    }
}

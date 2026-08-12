package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractAssignment;
import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.dto.ContractAssignmentCreateRequest;
import com.organization.contractmanager.dto.ContractAssignmentResponse;
import com.organization.contractmanager.dto.ContractAssignmentUpdateRequest;
import com.organization.contractmanager.exception.ContractAssignmentNotFoundException;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.exception.PersonNotFoundException;
import com.organization.contractmanager.mapper.ContractAssignmentMapper;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.PersonRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class ContractAssignmentService {

    private final ContractAssignmentRepository repository;
    private final ContractRepository contractRepository;
    private final PersonRepository personRepository;
    private final ContractAssignmentMapper mapper;
    private final ContractHistoryService historyService;

    public ContractAssignmentService(
            ContractAssignmentRepository repository,
            ContractRepository contractRepository,
            PersonRepository personRepository,
            ContractAssignmentMapper mapper,
            ContractHistoryService historyService) {
        this.repository = repository;
        this.contractRepository = contractRepository;
        this.personRepository = personRepository;
        this.mapper = mapper;
        this.historyService = historyService;
    }

    @Transactional(readOnly = true)
    public List<ContractAssignmentResponse> findByContract(UUID contractId) {
        requireContract(contractId);
        return repository.findAllByContractIdOrderByCreatedAtAsc(contractId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ContractAssignmentResponse create(
            UUID contractId, ContractAssignmentCreateRequest request) {
        Contract contract = requireContract(contractId);
        Person person = requirePerson(request.personId());
        return mapper.toResponse(assign(
                contract, person, request.role(), request.startDate(), request.endDate(),
                currentActor()));
    }

    @Transactional
    public ContractAssignmentResponse update(
            UUID contractId, UUID assignmentId, ContractAssignmentUpdateRequest request) {
        requireContract(contractId);
        ContractAssignment assignment = requireAssignment(contractId, assignmentId);
        if (!assignment.isActive()) {
            throw new IllegalStateException("Inactive assignment cannot be changed");
        }
        Person person = requirePerson(request.personId());
        if (!person.isActive()) {
            throw new IllegalStateException("Inactive person cannot receive an assignment");
        }
        validateDateRange(request.startDate(), request.endDate());
        assignment.update(person, request.role(), request.startDate(), request.endDate());
        return mapper.toResponse(repository.save(assignment));
    }

    @Transactional
    public ContractAssignmentResponse remove(
            UUID contractId, UUID assignmentId, LocalDate endDate) {
        requireContract(contractId);
        ContractAssignment assignment = requireAssignment(contractId, assignmentId);
        LocalDate effectiveEndDate = endDate == null ? LocalDate.now() : endDate;
        if (!assignment.isActive()) {
            throw new IllegalStateException("Assignment is already inactive");
        }
        validateDateRange(assignment.getStartDate(), effectiveEndDate);
        assignment.end(effectiveEndDate);
        ContractAssignment saved = repository.save(assignment);
        historyService.record(contractId, currentActor(), "CONTRACT_ASSIGNMENT",
                saved.getId(), HistoryAction.REMOVE_ASSIGNMENT,
                "active=true", "active=false;endDate=" + effectiveEndDate);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ContractAssignment assign(Contract contract, Person person, ContractRole role,
                                     LocalDate startDate, LocalDate endDate, String createdBy) {
        Objects.requireNonNull(contract, "Contract is required");
        Objects.requireNonNull(person, "Person is required");
        Objects.requireNonNull(role, "Role is required");
        Objects.requireNonNull(startDate, "Start date is required");
        requireText(createdBy, "Created by is required");

        if (!person.isActive()) {
            throw new IllegalStateException("Inactive person cannot receive a new assignment");
        }
        validateDateRange(startDate, endDate);

        ContractAssignment saved = repository.save(new ContractAssignment(
                contract, person, role, startDate, endDate, createdBy));
        historyService.record(contract.getId(), createdBy, "CONTRACT_ASSIGNMENT",
                saved.getId(), HistoryAction.ADD_ASSIGNMENT, null,
                "role=" + role + ";startDate=" + startDate + ";endDate=" + endDate);
        return saved;
    }

    @Transactional
    public ContractAssignment endAssignment(UUID assignmentId, LocalDate endDate) {
        Objects.requireNonNull(assignmentId, "Assignment id is required");
        Objects.requireNonNull(endDate, "End date is required");

        ContractAssignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
        if (!assignment.isActive()) {
            throw new IllegalStateException("Assignment is already inactive");
        }
        validateDateRange(assignment.getStartDate(), endDate);

        assignment.end(endDate);
        ContractAssignment saved = repository.save(assignment);
        historyService.record(saved.getContract().getId(), currentActor(),
                "CONTRACT_ASSIGNMENT", saved.getId(), HistoryAction.REMOVE_ASSIGNMENT,
                "active=true", "active=false;endDate=" + endDate);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ContractAssignment> findHistoryByContract(UUID contractId) {
        Objects.requireNonNull(contractId, "Contract id is required");
        return repository.findAllByContractIdOrderByCreatedAtAsc(contractId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private Contract requireContract(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    private Person requirePerson(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));
    }

    private ContractAssignment requireAssignment(UUID contractId, UUID assignmentId) {
        return repository.findByIdAndContractId(assignmentId, contractId)
                .orElseThrow(() -> new ContractAssignmentNotFoundException(assignmentId, contractId));
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated()
                ? "system" : authentication.getName();
    }
}

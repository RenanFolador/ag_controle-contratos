package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.dto.ContractResponse;
import com.organization.contractmanager.dto.ContractSummaryResponse;
import com.organization.contractmanager.dto.ContractUpdateRequest;
import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.dto.ContractHistoryResponse;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.exception.DuplicateContractNumberException;
import com.organization.contractmanager.exception.InvalidContractDateRangeException;
import com.organization.contractmanager.mapper.ContractMapper;
import com.organization.contractmanager.repository.ContractRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import static com.organization.contractmanager.repository.ContractSpecifications.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractService {

    private final ContractRepository repository;
    private final ContractMapper mapper;
    private final Clock clock;
    private final NotificationScheduleService notificationScheduleService;
    private final ContractHistoryService historyService;

    @Autowired
    public ContractService(
            ContractRepository repository, ContractMapper mapper,
            NotificationScheduleService notificationScheduleService,
            ContractHistoryService historyService) {
        this(repository, mapper, notificationScheduleService, historyService,
                Clock.systemDefaultZone());
    }

    ContractService(
            ContractRepository repository, ContractMapper mapper,
            NotificationScheduleService notificationScheduleService,
            ContractHistoryService historyService, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationScheduleService = notificationScheduleService;
        this.historyService = historyService;
        this.clock = clock;
    }

    @Transactional
    public ContractResponse create(ContractCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        ensureNumberAvailable(request.contractNumber(), null);

        Contract contract = mapper.toEntity(request, currentActor());
        Contract saved = saveTranslatingDuplicate(contract);
        historyService.record(saved.getId(), currentActor(), "CONTRACT", saved.getId(),
                HistoryAction.CREATE_CONTRACT, null, snapshot(saved));
        notificationScheduleService.createForActiveContract(saved);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContractSummaryResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "contractNumber")).stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> findAll(
            int page, int size, String sort, String search, ContractStatus status,
            Integer year, UUID personId, Integer expirationDays) {
        Sort sorting = parseSort(sort);
        Specification<Contract> specification = Specification.allOf(
                textContains(search), hasStatus(status), endsInYear(year),
                assignedTo(personId), expiresWithin(expirationDays, LocalDate.now(clock)));

        var result = repository.findAll(specification, PageRequest.of(page, size, sorting));
        return new PageResponse<>(
                result.getContent().stream().map(mapper::toSummary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Transactional(readOnly = true)
    public ContractResponse findById(UUID id) {
        return mapper.toResponse(getContract(id));
    }

    @Transactional(readOnly = true)
    public List<ContractHistoryResponse> findHistory(UUID id) {
        getContract(id);
        return historyService.findByContract(id);
    }

    @Transactional
    public ContractResponse update(UUID id, ContractUpdateRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        Contract contract = getContract(id);
        ensureNumberAvailable(request.contractNumber(), id);
        LocalDate previousEndDate = contract.getEndDate();
        String previousValue = snapshot(contract);
        boolean expirationDateChanged = !Objects.equals(previousEndDate, request.endDate());

        mapper.update(contract, request, currentActor());
        Contract saved = saveTranslatingDuplicate(contract);
        historyService.record(saved.getId(), currentActor(), "CONTRACT", saved.getId(),
                HistoryAction.UPDATE_CONTRACT, previousValue, snapshot(saved));
        if (expirationDateChanged) {
            historyService.record(saved.getId(), currentActor(), "CONTRACT", saved.getId(),
                    HistoryAction.CHANGE_EXPIRATION_DATE,
                    "endDate=" + previousEndDate, "endDate=" + saved.getEndDate());
            notificationScheduleService.rescheduleForExpirationChange(saved, previousEndDate);
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    public ContractResponse close(UUID id) {
        return changeStatus(id, ContractStatus.CLOSED);
    }

    @Transactional
    public ContractResponse cancel(UUID id) {
        return changeStatus(id, ContractStatus.CANCELLED);
    }

    private ContractResponse changeStatus(UUID id, ContractStatus status) {
        Contract contract = getContract(id);
        ContractStatus previousStatus = contract.getStatus();
        contract.setStatus(status);
        contract.setUpdatedBy(currentActor());
        Contract saved = repository.save(contract);
        HistoryAction action = status == ContractStatus.CLOSED
                ? HistoryAction.CLOSE_CONTRACT : HistoryAction.CANCEL_CONTRACT;
        historyService.record(id, currentActor(), "CONTRACT", id, action,
                "status=" + previousStatus, "status=" + status);
        return mapper.toResponse(saved);
    }

    private Contract getContract(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ContractNotFoundException(id));
    }

    private void ensureNumberAvailable(String contractNumber, UUID currentId) {
        repository.findByContractNumber(contractNumber).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new DuplicateContractNumberException(contractNumber);
            }
        });
    }

    private Contract saveTranslatingDuplicate(Contract contract) {
        try {
            return repository.saveAndFlush(contract);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateContractNumberException(contract.getContractNumber());
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidContractDateRangeException();
        }
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }

    private Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "contractNumber,asc" : sort.trim();
        String[] parts = value.split(",", -1);
        String property = parts[0];
        var allowed = java.util.Set.of(
                "contractNumber", "processNumber", "companyName", "startDate",
                "endDate", "initialValue", "status", "createdAt", "updatedAt");
        if (!allowed.contains(property) || parts.length > 2) {
            throw new IllegalArgumentException("Invalid sort: " + sort);
        }
        Sort.Direction direction;
        try {
            direction = parts.length == 2
                    ? Sort.Direction.fromString(parts[1]) : Sort.Direction.ASC;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid sort: " + sort);
        }
        return Sort.by(direction, property);
    }

    private String snapshot(Contract contract) {
        return "contractNumber=" + contract.getContractNumber()
                + ";startDate=" + contract.getStartDate()
                + ";endDate=" + contract.getEndDate()
                + ";status=" + contract.getStatus();
    }
}

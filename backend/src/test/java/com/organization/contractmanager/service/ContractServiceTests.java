package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.dto.ContractUpdateRequest;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.exception.DuplicateContractNumberException;
import com.organization.contractmanager.exception.InvalidContractDateRangeException;
import com.organization.contractmanager.mapper.ContractMapper;
import com.organization.contractmanager.repository.ContractRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ContractServiceTests {

    @Mock
    private ContractRepository repository;

    @Mock
    private NotificationScheduleService notificationScheduleService;

    @Mock
    private ContractHistoryService historyService;

    private ContractService service;

    @BeforeEach
    void setUp() {
        service = new ContractService(
                repository, new ContractMapper(), notificationScheduleService, historyService);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("manager", "n/a", java.util.List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsActiveContractAndRecordsActor() {
        when(repository.findByContractNumber("025/2026")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(createRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        assertThat(response.status()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(response.createdBy()).isEqualTo("manager");
        verify(repository).saveAndFlush(any(Contract.class));
        verify(notificationScheduleService).createForActiveContract(any(Contract.class));
        verify(historyService).record(any(), org.mockito.ArgumentMatchers.eq("manager"),
                org.mockito.ArgumentMatchers.eq("CONTRACT"), any(),
                org.mockito.ArgumentMatchers.eq(HistoryAction.CREATE_CONTRACT),
                org.mockito.ArgumentMatchers.isNull(), any());
    }

    @Test
    void rejectsDuplicateNumberBeforeSaving() {
        when(repository.findByContractNumber("025/2026"))
                .thenReturn(Optional.of(contract("025/2026")));

        assertThatThrownBy(() -> service.create(createRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))))
                .isInstanceOf(DuplicateContractNumberException.class);
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThatThrownBy(() -> service.create(createRequest(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31))))
                .isInstanceOf(InvalidContractDateRangeException.class);
    }

    @Test
    void reportsMissingContract() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void closesExistingContract() {
        UUID id = UUID.randomUUID();
        Contract contract = contract("025/2026");
        when(repository.findById(id)).thenReturn(Optional.of(contract));
        when(repository.save(contract)).thenReturn(contract);

        var response = service.close(id);

        assertThat(response.status()).isEqualTo(ContractStatus.CLOSED);
        assertThat(response.updatedBy()).isEqualTo("manager");
    }

    @Test
    void unchangedExpirationDoesNotRescheduleNotifications() {
        UUID id = UUID.randomUUID();
        Contract contract = contract("025/2026");
        when(repository.findById(id)).thenReturn(Optional.of(contract));
        when(repository.findByContractNumber("025/2026")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(contract)).thenReturn(contract);

        service.update(id, updateRequest(contract.getEndDate()));

        verify(notificationScheduleService, never())
                .rescheduleForExpirationChange(any(), any());
    }

    @Test
    void changedExpirationReschedulesUsingPreviousDate() {
        UUID id = UUID.randomUUID();
        Contract contract = contract("025/2026");
        LocalDate previousEndDate = contract.getEndDate();
        LocalDate newEndDate = previousEndDate.plusMonths(3);
        when(repository.findById(id)).thenReturn(Optional.of(contract));
        when(repository.findByContractNumber("025/2026")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(contract)).thenReturn(contract);

        service.update(id, updateRequest(newEndDate));

        assertThat(contract.getEndDate()).isEqualTo(newEndDate);
        verify(notificationScheduleService)
                .rescheduleForExpirationChange(contract, previousEndDate);
        verify(historyService).record(any(), org.mockito.ArgumentMatchers.eq("manager"),
                org.mockito.ArgumentMatchers.eq("CONTRACT"), any(),
                org.mockito.ArgumentMatchers.eq(HistoryAction.CHANGE_EXPIRATION_DATE),
                any(), any());
    }

    private ContractCreateRequest createRequest(LocalDate startDate, LocalDate endDate) {
        return new ContractCreateRequest(
                "025/2026", "PROCESS-1", "Objeto", "Empresa", null,
                startDate, endDate, new BigDecimal("100.00"), null, null);
    }

    private Contract contract(String number) {
        return new Contract(
                number, "PROCESS-1", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "creator");
    }

    private ContractUpdateRequest updateRequest(LocalDate endDate) {
        return new ContractUpdateRequest(
                "025/2026", "PROCESS-1", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), endDate, new BigDecimal("100.00"),
                ContractStatus.ACTIVE, null);
    }
}

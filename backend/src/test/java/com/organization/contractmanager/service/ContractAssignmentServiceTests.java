package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.dto.ContractAssignmentCreateRequest;
import com.organization.contractmanager.exception.ContractNotFoundException;
import com.organization.contractmanager.exception.PersonNotFoundException;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.PersonRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ContractAssignmentServiceTests {

    @Autowired
    private ContractAssignmentService service;

    @Autowired
    private ContractAssignmentRepository assignmentRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PersonRepository personRepository;

    private Contract contract;
    private Person activePerson;

    @BeforeEach
    void setUp() {
        contract = contractRepository.save(contract("100/2026"));
        activePerson = personRepository.save(person("Pessoa Ativa", "111.111.111-11", true));
    }

    @Test
    void assignsMultiplePeopleToTheSameContract() {
        Person anotherPerson = personRepository.save(
                person("Outra Pessoa", "222.222.222-22", true));

        service.assign(contract, activePerson, ContractRole.MANAGER,
                LocalDate.of(2026, 1, 1), null, "admin");
        service.assign(contract, anotherPerson, ContractRole.PRIMARY_INSPECTOR,
                LocalDate.of(2026, 1, 1), null, "admin");

        assertThat(assignmentRepository.findAllByContractIdAndActiveTrue(contract.getId()))
                .hasSize(2)
                .extracting(assignment -> assignment.getRole())
                .containsExactlyInAnyOrder(ContractRole.MANAGER, ContractRole.PRIMARY_INSPECTOR);
    }

    @Test
    void rejectsAssignmentForInactivePerson() {
        Person inactivePerson = personRepository.save(
                person("Pessoa Inativa", "333.333.333-33", false));

        assertThatThrownBy(() -> service.assign(
                contract, inactivePerson, ContractRole.SUBSTITUTE_INSPECTOR,
                LocalDate.of(2026, 1, 1), null, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Inactive person");
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertThatThrownBy(() -> service.assign(
                contract, activePerson, ContractRole.MANAGER,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void endsAssignmentWithoutDeletingHistory() {
        var assignment = service.assign(
                contract, activePerson, ContractRole.MANAGER,
                LocalDate.of(2026, 1, 1), null, "admin");
        assignmentRepository.flush();

        service.endAssignment(assignment.getId(), LocalDate.of(2026, 6, 30));
        assignmentRepository.flush();

        var history = service.findHistoryByContract(contract.getId());
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().isActive()).isFalse();
        assertThat(history.getFirst().getEndDate()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(assignmentRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidDateWhenEndingAssignment() {
        var assignment = service.assign(
                contract, activePerson, ContractRole.MANAGER,
                LocalDate.of(2026, 2, 1), null, "admin");
        assignmentRepository.flush();

        assertThatThrownBy(() -> service.endAssignment(
                assignment.getId(), LocalDate.of(2026, 1, 31)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void createsAssignmentResolvingContractAndPersonById() {
        var response = service.create(contract.getId(), new ContractAssignmentCreateRequest(
                activePerson.getId(), ContractRole.PRIMARY_INSPECTOR,
                LocalDate.of(2026, 1, 1), null));

        assertThat(response.contractId()).isEqualTo(contract.getId());
        assertThat(response.person().id()).isEqualTo(activePerson.getId());
        assertThat(response.person().name()).isEqualTo("Pessoa Ativa");
    }

    @Test
    void rejectsUnknownContract() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(missingId, new ContractAssignmentCreateRequest(
                activePerson.getId(), ContractRole.MANAGER,
                LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(ContractNotFoundException.class);
    }

    @Test
    void rejectsUnknownPerson() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(contract.getId(), new ContractAssignmentCreateRequest(
                missingId, ContractRole.MANAGER, LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(PersonNotFoundException.class);
    }

    private Contract contract(String number) {
        return new Contract(
                number, "PROCESS-1", "Objeto do contrato", "Empresa Exemplo",
                "12.345.678/0001-90", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), new BigDecimal("1000.00"),
                ContractStatus.ACTIVE, null, "admin");
    }

    private Person person(String name, String cpf, boolean active) {
        return new Person(name, cpf, "REG-1", name.replace(" ", "").toLowerCase() + "@example.com",
                null, false, active);
    }
}

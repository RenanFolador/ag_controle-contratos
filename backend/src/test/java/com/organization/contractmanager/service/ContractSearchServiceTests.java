package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractRole;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.PersonRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ContractSearchServiceTests {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractAssignmentService assignmentService;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PersonRepository personRepository;

    private Contract alpha;
    private Contract beta;
    private Person person;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        alpha = contractRepository.save(contract(
                "SEARCH-001", "PROC-ALPHA", "Serviços de nuvem", "Alpha Tecnologia",
                "11.111.111/0001-11", today.minusMonths(1), today.plusDays(15),
                ContractStatus.ACTIVE));
        beta = contractRepository.save(contract(
                "SEARCH-002", "PROC-BETA", "Manutenção predial", "Beta Engenharia",
                "22.222.222/0001-22", LocalDate.of(2025, 1, 1),
                LocalDate.of(2027, 12, 31), ContractStatus.SUSPENDED));
        person = personRepository.save(new Person(
                "Fiscal Pesquisa", "999.999.999-99", "SEARCH-PERSON",
                "fiscal.search@example.com", null, false, true));
        assignmentService.assign(
                alpha, person, ContractRole.PRIMARY_INSPECTOR, today.minusMonths(1), null, "test");
        contractRepository.flush();
    }

    @Test
    void combinesTextAndStatusFiltersInDatabase() {
        var page = contractService.findAll(
                0, 20, "contractNumber,asc", "nuvem", ContractStatus.ACTIVE,
                null, null, null);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content().getFirst().contractNumber()).isEqualTo("SEARCH-001");
    }

    @Test
    void filtersByExpirationYearAndPerson() {
        int year = alpha.getEndDate().getYear();

        var page = contractService.findAll(
                0, 20, "endDate,asc", null, null, year, person.getId(), null);

        assertThat(page.content()).extracting(item -> item.contractNumber())
                .containsExactly("SEARCH-001");
    }

    @Test
    void filtersContractsExpiringWithinDays() {
        var page = contractService.findAll(
                0, 20, "endDate,asc", null, null, null, null, 30);

        assertThat(page.content()).extracting(item -> item.contractNumber())
                .contains("SEARCH-001")
                .doesNotContain("SEARCH-002");
    }

    @Test
    void paginatesAndSortsWithoutLoadingAllRows() {
        var page = contractService.findAll(
                0, 1, "contractNumber,desc", "SEARCH-", null,
                null, null, null);

        assertThat(page.size()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content().getFirst().contractNumber()).isEqualTo("SEARCH-002");
    }

    private Contract contract(
            String number, String process, String object, String company, String cnpj,
            LocalDate startDate, LocalDate endDate, ContractStatus status) {
        return new Contract(
                number, process, object, company, cnpj, startDate, endDate,
                new BigDecimal("1000.00"), status, null, "test");
    }
}

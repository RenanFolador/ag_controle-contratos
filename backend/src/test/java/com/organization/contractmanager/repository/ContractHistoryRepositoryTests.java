package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractHistory;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.HistoryAction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class ContractHistoryRepositoryTests {

    @Autowired private ContractHistoryRepository historyRepository;
    @Autowired private ContractRepository contractRepository;

    @Test
    void persistsAndListsSanitizedContractHistory() {
        Contract contract = contractRepository.saveAndFlush(new Contract(
                "HISTORY-001", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "creator"));
        historyRepository.saveAndFlush(new ContractHistory(
                contract.getId(), "manager", "CONTRACT", contract.getId(),
                HistoryAction.CHANGE_EXPIRATION_DATE,
                "endDate=2026-12-31", "endDate=2027-03-31"));

        var history = historyRepository
                .findAllByContractIdOrderByTimestampDesc(contract.getId());

        assertThat(history).singleElement().satisfies(item -> {
            assertThat(item.getAction()).isEqualTo(HistoryAction.CHANGE_EXPIRATION_DATE);
            assertThat(item.getActor()).isEqualTo("manager");
            assertThat(item.getOldValue()).doesNotContain("password", "cpf", "email");
            assertThat(item.getTimestamp()).isNotNull();
        });
    }
}

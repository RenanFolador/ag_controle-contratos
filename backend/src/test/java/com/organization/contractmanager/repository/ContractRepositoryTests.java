package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class ContractRepositoryTests {

    @Autowired
    private ContractRepository repository;

    @Test
    void persistsAndFindsContractByNumber() {
        Contract saved = repository.saveAndFlush(contract("025/2026"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(repository.findByContractNumber("025/2026"))
                .get()
                .extracting(Contract::getCompanyName, Contract::getStatus)
                .containsExactly("Empresa Exemplo S.A.", ContractStatus.ACTIVE);
    }

    @Test
    void rejectsDuplicateContractNumber() {
        repository.saveAndFlush(contract("025/2026"));

        assertThatThrownBy(() -> repository.saveAndFlush(contract("025/2026")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatesAuditTimestampAndActor() {
        Contract saved = repository.saveAndFlush(contract("026/2026"));
        var originalUpdatedAt = saved.getUpdatedAt();

        saved.setNotes("Vigência revisada");
        saved.setUpdatedBy("editor@example.com");
        repository.saveAndFlush(saved);

        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        assertThat(saved.getUpdatedBy()).isEqualTo("editor@example.com");
        assertThat(saved.getCreatedBy()).isEqualTo("creator@example.com");
    }

    private Contract contract(String number) {
        return new Contract(
                number,
                "00001.000001/2026-01",
                "Prestação de serviços especializados",
                "Empresa Exemplo S.A.",
                "12.345.678/0001-90",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("125000.50"),
                ContractStatus.ACTIVE,
                null,
                "creator@example.com");
    }
}

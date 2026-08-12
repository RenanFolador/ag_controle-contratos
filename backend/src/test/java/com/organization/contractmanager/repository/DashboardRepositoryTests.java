package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class DashboardRepositoryTests {
    @Autowired private ContractRepository repository;

    @Test
    void aggregatesActiveExpiredAndCumulativeExpirationRangesInDatabase() {
        LocalDate today = LocalDate.of(2026, 8, 12);
        AtomicInteger sequence = new AtomicInteger();
        repository.saveAll(java.util.List.of(
                contract(sequence, today.minusDays(1), ContractStatus.ACTIVE),
                contract(sequence, today, ContractStatus.ACTIVE),
                contract(sequence, today.plusDays(10), ContractStatus.ACTIVE),
                contract(sequence, today.plusDays(20), ContractStatus.ACTIVE),
                contract(sequence, today.plusDays(50), ContractStatus.ACTIVE),
                contract(sequence, today.plusDays(70), ContractStatus.ACTIVE),
                contract(sequence, today.plusDays(10), ContractStatus.CLOSED)));
        repository.flush();

        var counts = repository.dashboardCounts(
                today, today.plusDays(15), today.plusDays(30), today.plusDays(60));

        assertThat(counts.activeContracts()).isEqualTo(6);
        assertThat(counts.expiredContracts()).isEqualTo(1);
        assertThat(counts.expiringIn15Days()).isEqualTo(2);
        assertThat(counts.expiringIn30Days()).isEqualTo(3);
        assertThat(counts.expiringIn60Days()).isEqualTo(4);
    }

    private Contract contract(
            AtomicInteger sequence, LocalDate endDate, ContractStatus status) {
        int number = sequence.incrementAndGet();
        return new Contract(
                "DASH-" + number, "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), endDate, new BigDecimal("100.00"),
                status, null, "test");
    }
}

package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.report.ReportData;
import com.organization.contractmanager.report.ReportExporter;
import com.organization.contractmanager.report.ReportFilter;
import com.organization.contractmanager.report.ReportFormat;
import com.organization.contractmanager.report.ReportType;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

class ReportServiceTests {
    private ContractRepository contracts;
    private ReportService service;

    @BeforeEach
    void setUp() {
        contracts = org.mockito.Mockito.mock(ContractRepository.class);
        ContractAssignmentRepository assignments =
                org.mockito.Mockito.mock(ContractAssignmentRepository.class);
        NotificationRepository notifications =
                org.mockito.Mockito.mock(NotificationRepository.class);
        ReportExporter exporter = new ReportExporter() {
            public ReportFormat getFormat() { return ReportFormat.CSV; }
            public String getContentType() { return "text/csv"; }
            public String getExtension() { return "csv"; }
            public byte[] export(ReportData data) {
                return (data.headers().size() + ":" + data.rows().size())
                        .getBytes(StandardCharsets.UTF_8);
            }
        };
        service = new ReportService(contracts, assignments, notifications, List.of(exporter),
                Clock.fixed(Instant.parse("2026-08-12T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void exportsActiveContractsUsingConfiguredExporter() {
        Contract contract = new Contract(
                "025/2026", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100"), ContractStatus.ACTIVE, null, "test");
        when(contracts.findAll(
                org.mockito.ArgumentMatchers.<Specification<Contract>>any(),
                any(Sort.class)))
                .thenReturn(List.of(contract));

        var file = service.export(ReportType.ACTIVE_CONTRACTS, ReportFormat.CSV,
                new ReportFilter(2026, null, null, null, null, "Empresa"));

        assertThat(new String(file.content(), StandardCharsets.UTF_8)).isEqualTo("9:1");
        assertThat(file.filename()).isEqualTo("active_contracts-2026-08-12.csv");
        assertThat(file.contentType()).isEqualTo("text/csv");
    }

    @Test
    void rejectsUnsupportedFormatsAndInvalidPeriods() {
        ReportFilter filter = new ReportFilter(null, null,
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 1), null, null);
        assertThatThrownBy(() -> service.export(
                ReportType.ACTIVE_CONTRACTS, ReportFormat.CSV, filter))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.export(
                ReportType.ACTIVE_CONTRACTS, ReportFormat.PDF,
                new ReportFilter(null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ainda não suportado");
    }
}

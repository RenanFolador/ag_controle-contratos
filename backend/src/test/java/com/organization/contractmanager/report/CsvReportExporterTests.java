package com.organization.contractmanager.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvReportExporterTests {
    private final CsvReportExporter exporter = new CsvReportExporter();

    @Test
    void exportsUtf8WithBomAndEscapesDelimitedValues() {
        byte[] result = exporter.export(new ReportData(
                List.of("Contrato", "Empresa"),
                List.of(List.of("025/2026", "Empresa; com \"aspas\""))));

        String csv = new String(result, StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFFContrato;Empresa\r\n")
                .contains("025/2026;\"Empresa; com \"\"aspas\"\"\"\r\n");
    }
}

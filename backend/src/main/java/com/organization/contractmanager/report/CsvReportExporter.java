package com.organization.contractmanager.report;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CsvReportExporter implements ReportExporter {
    @Override
    public ReportFormat getFormat() {
        return ReportFormat.CSV;
    }

    @Override
    public String getContentType() {
        return "text/csv;charset=UTF-8";
    }

    @Override
    public String getExtension() {
        return "csv";
    }

    @Override
    public byte[] export(ReportData data) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, data.headers());
        data.rows().forEach(row -> appendRow(csv, row));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, Iterable<?> values) {
        String line = java.util.stream.StreamSupport.stream(values.spliterator(), false)
                .map(this::escape)
                .collect(Collectors.joining(";"));
        csv.append(line).append("\r\n");
    }

    private String escape(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(";") || text.contains("\"")
                || text.contains("\r") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}

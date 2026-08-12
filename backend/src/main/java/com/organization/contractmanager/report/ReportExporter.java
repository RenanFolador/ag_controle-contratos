package com.organization.contractmanager.report;

public interface ReportExporter {
    ReportFormat getFormat();

    String getContentType();

    String getExtension();

    byte[] export(ReportData data);
}

package com.organization.contractmanager.report;

public record ReportFile(byte[] content, String contentType, String filename) {
}

package com.organization.contractmanager.report;

import java.util.List;

public record ReportData(List<String> headers, List<List<?>> rows) {
}

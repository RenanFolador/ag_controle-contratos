package com.organization.contractmanager.controller;

import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.report.ReportFile;
import com.organization.contractmanager.report.ReportFilter;
import com.organization.contractmanager.report.ReportFormat;
import com.organization.contractmanager.report.ReportType;
import com.organization.contractmanager.service.ReportService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam ReportType type,
            @RequestParam(defaultValue = "CSV") ReportFormat format,
            @RequestParam(required = false) @Min(1900) @Max(9999) Integer year,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) @Size(max = 255) String company) {
        ReportFile file = service.export(type, format,
                new ReportFilter(year, status, startDate, endDate, personId, company));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.filename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .body(file.content());
    }
}

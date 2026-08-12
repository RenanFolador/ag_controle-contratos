package com.organization.contractmanager.controller;

import com.organization.contractmanager.dto.DashboardResponse;
import com.organization.contractmanager.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping
    public DashboardResponse getDashboard() { return service.getDashboard(); }
}

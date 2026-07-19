package com.vasyerp.Controller;

import com.vasyerp.Model.DashboardSummary;
import com.vasyerp.Service.ReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ReportService reportService;

    public DashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/summary")
    public DashboardSummary getSummary() {
        return reportService.getSummary();
    }
}
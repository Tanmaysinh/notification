
package com.vasyerp.Controller;

import com.vasyerp.Model.ReportFilterRequest;
import com.vasyerp.Model.ReportPage;
import com.vasyerp.Model.RetryRequest;
import com.vasyerp.Service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/list")
    public ReportPage list(@RequestBody ReportFilterRequest filter) {
        return reportService.getReport(filter);
    }

    @PostMapping("/retry")
    public Map<String, Boolean> retry(@RequestBody RetryRequest request) {
        reportService.retry(request.getRequestId(), request.getContactId(),request.getChannelType());
        return Map.of("retried", true);
    }
}
package com.vasyerp.Service;

import com.vasyerp.Component.NotificationListener;
import com.vasyerp.Model.DashboardSummary;
import com.vasyerp.Model.ReportFilterRequest;
import com.vasyerp.Model.ReportPage;
import com.vasyerp.Model.ReportRow;

import java.util.Map;

public interface ReportService {
    ReportPage getReport(ReportFilterRequest filter);
    void retry(String requestId, String contactId, String channelType);
    DashboardSummary getSummary();


}

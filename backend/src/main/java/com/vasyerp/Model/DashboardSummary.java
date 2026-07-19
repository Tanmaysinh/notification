package com.vasyerp.Model;


import java.util.List;
import java.util.Map;

public class DashboardSummary {
    private long totalRequests;
    private long totalRecipients;
    private Map<String, Long> requestsByStatus;       // {"SCHEDULED": 3, "PROCESSING": 1, "COMPLETED": 42}
    private Map<String, Map<String, Long>> statusByChannel; // {"sms": {"SENT":100,"DELIVERED":90,"FAILED":10}, ...}
    private Map<String, Long> retriesByChannel;        // {"sms": 12, "email": 3, "push": 0}
    private List<DailyCount> requestsOverTime;         // last 14 days, for a trend chart

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
    public long getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(long totalRecipients) { this.totalRecipients = totalRecipients; }
    public Map<String, Long> getRequestsByStatus() { return requestsByStatus; }
    public void setRequestsByStatus(Map<String, Long> requestsByStatus) { this.requestsByStatus = requestsByStatus; }
    public Map<String, Map<String, Long>> getStatusByChannel() { return statusByChannel; }
    public void setStatusByChannel(Map<String, Map<String, Long>> statusByChannel) { this.statusByChannel = statusByChannel; }
    public Map<String, Long> getRetriesByChannel() { return retriesByChannel; }
    public void setRetriesByChannel(Map<String, Long> retriesByChannel) { this.retriesByChannel = retriesByChannel; }
    public List<DailyCount> getRequestsOverTime() { return requestsOverTime; }
    public void setRequestsOverTime(List<DailyCount> requestsOverTime) { this.requestsOverTime = requestsOverTime; }

    public static class DailyCount {
        private final String date;
        private final long count;

        public DailyCount(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public long getCount() { return count; }
    }
}
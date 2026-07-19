package com.vasyerp.Model;

import java.time.Instant;

public class ReportFilterRequest {
    private Instant dateFrom;
    private Instant dateTo;
    private String requestId;
    private String notificationType;   // sms | email | push
    private String requestStatus;      // NotificationRequest.status: SCHEDULED, PROCESSING, COMPLETED...
    private String notificationStatus; // per-contact status: SENT, FAILED, PENDING...
    private String contactSearch;      // matches contact phone or email
    private String campaignId;
    private int page = 0;
    private int size = 10;

    public Instant getDateFrom() { return dateFrom; }
    public void setDateFrom(Instant dateFrom) { this.dateFrom = dateFrom; }
    public Instant getDateTo() { return dateTo; }
    public void setDateTo(Instant dateTo) { this.dateTo = dateTo; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    public String getNotificationStatus() { return notificationStatus; }
    public void setNotificationStatus(String notificationStatus) { this.notificationStatus = notificationStatus; }
    public String getContactSearch() { return contactSearch; }
    public void setContactSearch(String contactSearch) { this.contactSearch = contactSearch; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    @Override
    public String toString() {
        return "ReportFilterRequest{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", requestId='" + requestId + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", requestStatus='" + requestStatus + '\'' +
                ", notificationStatus='" + notificationStatus + '\'' +
                ", contactSearch='" + contactSearch + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
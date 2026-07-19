package com.vasyerp.Model;

import java.time.Instant;
import java.util.List;

public class ReportRow {
    private String requestId;
    private String campaignId;
    private String campaignName;
    private Instant scheduleTime;
    private Instant createdAt;
    private String requestStatus;

    private String contactId;
    private String contactName;
    private String contactEmail;
    private String contactPhone;

    private List<ChannelRow> channels;

    // getters/setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public Instant getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(Instant scheduleTime) { this.scheduleTime = scheduleTime; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public List<ChannelRow> getChannels() { return channels; }
    public void setChannels(List<ChannelRow> channels) { this.channels = channels; }

    public static class ChannelRow {
        private String channelType; // sms | email | push
        private String content;    // resolved template content for that channel
        private List<String> statusHistory;
        private String latestStatus;
        private int retryCount;
        private boolean retryEligible;
        private String userData;

        public String getChannelType() { return channelType; }
        public void setChannelType(String channelType) { this.channelType = channelType; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public List<String> getStatusHistory() { return statusHistory; }
        public void setStatusHistory(List<String> statusHistory) { this.statusHistory = statusHistory; }
        public String getLatestStatus() { return latestStatus; }
        public void setLatestStatus(String latestStatus) { this.latestStatus = latestStatus; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
        public boolean isRetryEligible() { return retryEligible; }
        public void setRetryEligible(boolean retryEligible) { this.retryEligible = retryEligible; }

        public String getUserData() {
            return userData;
        }

        public void setUserData(String userData) {
            this.userData = userData;
        }

        @Override
        public String toString() {
            return "ChannelRow{" +
                    "channelType='" + channelType + '\'' +
                    ", content='" + content + '\'' +
                    ", statusHistory=" + statusHistory +
                    ", latestStatus='" + latestStatus + '\'' +
                    ", retryCount=" + retryCount +
                    ", retryEligible=" + retryEligible +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReportRow{" +
                "requestId='" + requestId + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", campaignName='" + campaignName + '\'' +
                ", scheduleTime=" + scheduleTime +
                ", createdAt=" + createdAt +
                ", requestStatus='" + requestStatus + '\'' +
                ", contactId='" + contactId + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", channels=" + channels +
                '}';
    }
}
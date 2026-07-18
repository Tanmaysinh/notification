package com.vasyerp.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "NotificationRequestMaster")
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String requestId;

    private String campaignId;
    private String smsTemplateId;
    private String emailTemplateId;
    private String pushTemplateId;

    @Column(nullable = false)
    private int recipientCount;

    @Column(nullable = false)
    private String status;

    private Instant scheduleTime; // null = send immediately

    @Column(nullable = false)
    private Instant createdAt;

    protected NotificationRequest() {}

    public NotificationRequest(String campaignId, String smsTemplateId, String emailTemplateId,
                               String pushTemplateId, int recipientCount, Instant scheduleTime) {
        this.campaignId = campaignId;
        this.smsTemplateId = smsTemplateId;
        this.emailTemplateId = emailTemplateId;
        this.pushTemplateId = pushTemplateId;
        this.recipientCount = recipientCount;
        this.status = "SCHEDULED";
        this.scheduleTime = scheduleTime;
        this.createdAt = Instant.now();
    }

    public String getRequestId() { return requestId; }
    public String getCampaignId() { return campaignId; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public String getEmailTemplateId() { return emailTemplateId; }
    public String getPushTemplateId() { return pushTemplateId; }
    public int getRecipientCount() { return recipientCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getScheduleTime() { return scheduleTime; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "requestId='" + requestId + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", smsTemplateId='" + smsTemplateId + '\'' +
                ", emailTemplateId='" + emailTemplateId + '\'' +
                ", pushTemplateId='" + pushTemplateId + '\'' +
                ", recipientCount=" + recipientCount +
                ", status='" + status + '\'' +
                ", scheduleTime=" + scheduleTime +
                ", createdAt=" + createdAt +
                '}';
    }
}
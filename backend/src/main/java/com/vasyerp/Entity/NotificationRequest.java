//package com.vasyerp.Entity;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(name = "NotificationRequestMaster")
//public class NotificationRequest {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String requestId;
//
//    private String campaignId;
//    private NotificationTemplate smsTemplate;
//    private NotificationTemplate emailTemplate;
//    private NotificationTemplate pushTemplate;
//
//    @Column(nullable = false)
//    private int recipientCount;
//
//    @Column(nullable = false)
//    private String status;
//
//    private Instant scheduleTime=Instant.now();
//
//    @Column(nullable = false)
//    private Instant createdAt;
//
//    @Column
//    private Boolean summaryFetched=false;
//
//    @Column
//    private Integer retryAttempt;
//
//    @Column
//    private Json Channelwiseretrycount;
//
//    @Column
//    private Json TotalChannelWiseStatuscount;
//
//    protected NotificationRequest() {}
//
//    public NotificationRequest(String campaignId, NotificationTemplate smsTemplateId, NotificationTemplate emailTemplateId,
//                               NotificationTemplate pushTemplateId, int recipientCount, Instant scheduleTime) {
//        this.campaignId = campaignId;
//        this.smsTemplate = smsTemplateId;
//        this.emailTemplate = emailTemplateId;
//        this.pushTemplate = pushTemplateId;
//        this.recipientCount = recipientCount;
//        this.status = "SCHEDULED";
//        this.scheduleTime = scheduleTime;
//        this.createdAt = Instant.now();
//    }
//
//    public String getRequestId() { return requestId; }
//    public String getCampaignId() { return campaignId; }
//    public NotificationTemplate getSmsTemplateId() { return smsTemplate; }
//    public NotificationTemplate getEmailTemplateId() { return emailTemplate; }
//    public NotificationTemplate getPushTemplateId() { return pushTemplate; }
//    public int getRecipientCount() { return recipientCount; }
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//    public Instant getScheduleTime() { return scheduleTime; }
//    public Instant getCreatedAt() { return createdAt; }
//
//    @Override
//    public String toString() {
//        return "NotificationRequest{" +
//                "requestId='" + requestId + '\'' +
//                ", campaignId='" + campaignId + '\'' +
//                ", smsTemplateId='" + smsTemplate+ '\'' +
//                ", emailTemplateId='" + emailTemplate + '\'' +
//                ", pushTemplateId='" + pushTemplate + '\'' +
//                ", recipientCount=" + recipientCount +
//                ", status='" + status + '\'' +
//                ", scheduleTime=" + scheduleTime +
//                ", createdAt=" + createdAt +
//                '}';
//    }
//}



package com.vasyerp.Entity;

import com.vasyerp.Component.NotificationTemplateConverter;
import com.vasyerp.Model.NotificationTemplateModel;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NotificationRequestMaster")
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String requestId;

    private String campaignId;


    @Convert(converter = NotificationTemplateConverter.class)
    @Column(name = "sms_template", columnDefinition = "TEXT")
    private NotificationTemplateModel smsTemplate;

    @Convert(converter = NotificationTemplateConverter.class)
    @Column(name = "email_template", columnDefinition = "TEXT")
    private NotificationTemplateModel emailTemplate;

    @Convert(converter = NotificationTemplateConverter.class)
    @Column(name = "push_template", columnDefinition = "TEXT")
    private NotificationTemplateModel pushTemplate;

    @Column(nullable = false)
    private int recipientCount;

    @Column(nullable = false)
    private String status; // SCHEDULED -> SENT -> PROCESSING -> COMPLETED

    private Instant scheduleTime = Instant.now();

    @Column(nullable = false)
    private Instant createdAt;

    private Boolean summaryFetched = false;

    private Integer retryAttempt = 3; // max retries allowed per channel for this request

    @Column(columnDefinition = "TEXT")
    private String channelWiseRetryCount; // {"sms": 4, "email": 1, "push": 0} — total retries used, filled in on completion

    @Column(columnDefinition = "TEXT")
    private String totalChannelWiseStatusCount; // {"sms": {"SENT":10,"DELIVERED":8,"FAILED":2}, ...} — cached summary

    @OneToMany(
            mappedBy = "request",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<RequestStatus> requestStatuses = new ArrayList<>();

    protected NotificationRequest() {}

    public NotificationRequest(String campaignId, NotificationTemplateModel smsTemplate, NotificationTemplateModel emailTemplate,
                               NotificationTemplateModel pushTemplate, int recipientCount, Instant scheduleTime) {
        this.campaignId = campaignId;
        this.smsTemplate = smsTemplate;
        this.emailTemplate = emailTemplate;
        this.pushTemplate = pushTemplate;
        this.recipientCount = recipientCount;
        this.status = "SCHEDULED";
        this.scheduleTime = scheduleTime != null ? scheduleTime : Instant.now();
        this.createdAt = Instant.now();
    }

    public String getRequestId() { return requestId; }
    public String getCampaignId() { return campaignId; }
    public NotificationTemplateModel getSmsTemplate() { return smsTemplate; }
    public NotificationTemplateModel getEmailTemplate() { return emailTemplate; }
    public NotificationTemplateModel getPushTemplate() { return pushTemplate; }
    public int getRecipientCount() { return recipientCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getScheduleTime() { return scheduleTime; }
    public Instant getCreatedAt() { return createdAt; }
    public Boolean getSummaryFetched() { return summaryFetched; }
    public void setSummaryFetched(Boolean summaryFetched) { this.summaryFetched = summaryFetched; }
    public Integer getRetryAttempt() { return retryAttempt; }
    public void setRetryAttempt(Integer retryAttempt) { this.retryAttempt = retryAttempt; }
    public String getChannelWiseRetryCount() { return channelWiseRetryCount; }
    public void setChannelWiseRetryCount(String channelWiseRetryCount) { this.channelWiseRetryCount = channelWiseRetryCount; }
    public String getTotalChannelWiseStatusCount() { return totalChannelWiseStatusCount; }
    public void setTotalChannelWiseStatusCount(String totalChannelWiseStatusCount) { this.totalChannelWiseStatusCount = totalChannelWiseStatusCount; }
    public List<RequestStatus> getRequestStatuses() {
        return requestStatuses;
    }

    public void setRequestStatuses(List<RequestStatus> requestStatuses) {
        this.requestStatuses = requestStatuses;
    }
}
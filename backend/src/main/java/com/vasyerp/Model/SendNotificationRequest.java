package com.vasyerp.Model;

import java.time.Instant;
import java.util.List;

public class SendNotificationRequest {
    private String campaignId;
    private String smsTemplateId;
    private String emailTemplateId;
    private String pushTemplateId;
    private List<String> contactIds;
    private Instant scheduleTime; // null or ISO string from frontend => sent immediately

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public void setSmsTemplateId(String smsTemplateId) { this.smsTemplateId = smsTemplateId; }
    public String getEmailTemplateId() { return emailTemplateId; }
    public void setEmailTemplateId(String emailTemplateId) { this.emailTemplateId = emailTemplateId; }
    public String getPushTemplateId() { return pushTemplateId; }
    public void setPushTemplateId(String pushTemplateId) { this.pushTemplateId = pushTemplateId; }
    public List<String> getContactIds() { return contactIds; }
    public void setContactIds(List<String> contactIds) { this.contactIds = contactIds; }
    public Instant getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(Instant scheduleTime) { this.scheduleTime = scheduleTime; }
}
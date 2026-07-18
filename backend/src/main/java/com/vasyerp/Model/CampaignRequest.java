package com.vasyerp.Model;

public class CampaignRequest {
    private String name;
    private String smsTemplateId;
    private String emailTemplateId;
    private String pushTemplateId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public void setSmsTemplateId(String smsTemplateId) { this.smsTemplateId = smsTemplateId; }
    public String getEmailTemplateId() { return emailTemplateId; }
    public void setEmailTemplateId(String emailTemplateId) { this.emailTemplateId = emailTemplateId; }
    public String getPushTemplateId() { return pushTemplateId; }
    public void setPushTemplateId(String pushTemplateId) { this.pushTemplateId = pushTemplateId; }
}
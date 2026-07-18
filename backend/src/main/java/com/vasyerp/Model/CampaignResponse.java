package com.vasyerp.Model;

public class CampaignResponse {
    private String campaignId;
    private String name;
    private String smsTemplateId;
    private String smsTemplateName;
    private String emailTemplateId;
    private String emailTemplateName;
    private String pushTemplateId;
    private String pushTemplateName;

    public CampaignResponse(String campaignId, String name,
                            String smsTemplateId, String smsTemplateName,
                            String emailTemplateId, String emailTemplateName,
                            String pushTemplateId, String pushTemplateName) {
        this.campaignId = campaignId;
        this.name = name;
        this.smsTemplateId = smsTemplateId;
        this.smsTemplateName = smsTemplateName;
        this.emailTemplateId = emailTemplateId;
        this.emailTemplateName = emailTemplateName;
        this.pushTemplateId = pushTemplateId;
        this.pushTemplateName = pushTemplateName;
    }

    public String getCampaignId() { return campaignId; }
    public String getName() { return name; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public String getSmsTemplateName() { return smsTemplateName; }
    public String getEmailTemplateId() { return emailTemplateId; }
    public String getEmailTemplateName() { return emailTemplateName; }
    public String getPushTemplateId() { return pushTemplateId; }
    public String getPushTemplateName() { return pushTemplateName; }
}
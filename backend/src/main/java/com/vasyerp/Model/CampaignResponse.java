package com.vasyerp.Model;

public class CampaignResponse {
    private final String campaignId;
    private final String name;
    private final String smsTemplateId;
    private final String smsTemplateName;
    private final String emailTemplateId;
    private final String emailTemplateName;
    private final String pushTemplateId;
    private final String pushTemplateName;

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
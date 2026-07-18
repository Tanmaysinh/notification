package com.vasyerp.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CampaignMaster")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String campaignId;

    @Column(nullable = false)
    private String name;

    private String smsTemplateId;
    private String emailTemplateId;
    private String pushTemplateId;

    protected Campaign() {}

    public Campaign(String name, String smsTemplateId, String emailTemplateId, String pushTemplateId) {
        this.name = name;
        this.smsTemplateId = smsTemplateId;
        this.emailTemplateId = emailTemplateId;
        this.pushTemplateId = pushTemplateId;
    }

    public String getCampaignId() { return campaignId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public void setSmsTemplateId(String smsTemplateId) { this.smsTemplateId = smsTemplateId; }
    public String getEmailTemplateId() { return emailTemplateId; }
    public void setEmailTemplateId(String emailTemplateId) { this.emailTemplateId = emailTemplateId; }
    public String getPushTemplateId() { return pushTemplateId; }
    public void setPushTemplateId(String pushTemplateId) { this.pushTemplateId = pushTemplateId; }
}
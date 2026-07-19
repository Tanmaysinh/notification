package com.vasyerp.Model;

public class NotificationTemplateModel {
    private String templateId;

    private String name;

    private String content;


    protected NotificationTemplateModel() {}

    public NotificationTemplateModel(String name, String content, String templateId) {
        this.name = name;
        this.content = content;
        this.templateId = templateId;
    }

    public String getTemplateId() { return templateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

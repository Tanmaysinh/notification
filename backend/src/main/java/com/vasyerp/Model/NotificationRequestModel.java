package com.vasyerp.Model;


import com.vasyerp.Entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

public class NotificationRequestModel {
    private String requestId;
    private Map<String, NotificationTemplateModel> templates; // e.g. {"sms": "...", "email": "...", "push": "..."}
    private List<ContactDto> contacts;

    public NotificationRequestModel() {}

    public NotificationRequestModel(String requestId, Map<String, NotificationTemplateModel> templates, List<ContactDto> contacts) {
        this.requestId = requestId;
        this.templates = templates;
        this.contacts = contacts;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Map<String, NotificationTemplateModel> getTemplates() { return templates; }
    public void setTemplates(Map<String, NotificationTemplateModel> templates) { this.templates = templates; }
    public List<ContactDto> getContacts() { return contacts; }
    public void setContacts(List<ContactDto> contacts) { this.contacts = contacts; }
}
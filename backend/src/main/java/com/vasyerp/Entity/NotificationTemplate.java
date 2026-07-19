package com.vasyerp.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String templateId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    NotificationTemplate() {}

    NotificationTemplate(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getTemplateId() { return templateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
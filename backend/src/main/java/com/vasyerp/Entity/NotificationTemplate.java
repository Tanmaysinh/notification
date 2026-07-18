package com.vasyerp.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Shared fields for SMS / Email / Push templates. @MappedSuperclass means
 * Hibernate inlines these columns into each subclass's own table — no
 * separate "base_template" table gets created, this is purely for OOP
 * reuse across the three template entities.
 */
@MappedSuperclass
public abstract class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String templateId;

    @Column(nullable = false)
    protected String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    protected String content;

    protected NotificationTemplate() {}

    protected NotificationTemplate(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getTemplateId() { return templateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
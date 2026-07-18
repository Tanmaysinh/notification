package com.vasyerp.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "EmailTemplateMaster")
public class EmailTemplate extends NotificationTemplate {

    protected EmailTemplate() { super(); }

    public EmailTemplate(String name, String content) {
        super(name, content);
    }
}
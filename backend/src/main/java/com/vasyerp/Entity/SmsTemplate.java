package com.vasyerp.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "SmsTemplateMaster")
public class SmsTemplate extends NotificationTemplate {

    protected SmsTemplate() { super(); }

    public SmsTemplate(String name, String content) {
        super(name, content);
    }
}
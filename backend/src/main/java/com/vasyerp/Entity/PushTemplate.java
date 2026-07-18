package com.vasyerp.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "PushTemplateMaster")
public class PushTemplate extends NotificationTemplate {

    protected PushTemplate() { super(); }

    public PushTemplate(String name, String content) {
        super(name, content);
    }
}
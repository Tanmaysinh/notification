package com.vasyerp.Service;

import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.TemplateRequest;
import org.springframework.data.domain.Page;

public interface SmsTemplateService {
    Page<NotificationTemplate> list(String search, int page, int size);
    NotificationTemplate create(TemplateRequest req);
    NotificationTemplate update(String id, TemplateRequest req);
    void delete(String id);
}


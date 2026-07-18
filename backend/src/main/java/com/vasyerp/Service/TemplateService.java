package com.vasyerp.Service;

import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.TemplateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TemplateService<T extends NotificationTemplate> {

    Page<T> list(String search, int page, int size);

    T create(TemplateRequest req);

    T update(String id, TemplateRequest req);

    void delete(String id);

    List<T> getAll();
}
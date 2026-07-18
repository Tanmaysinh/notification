package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.TemplateRequest;
import com.vasyerp.Repository.SmsTemplateRepository;
import com.vasyerp.Service.SmsTemplateService;
import com.vasyerp.Service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsTemplateServiceImpl implements TemplateService<SmsTemplate>  {
    private final SmsTemplateRepository repository;

    public SmsTemplateServiceImpl(SmsTemplateRepository repository) {
        this.repository = repository;
    }

    public Page<SmsTemplate> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size));
    }

    public List<SmsTemplate> getAll() {
        return repository.findAll();
    }

    public SmsTemplate create(TemplateRequest req) {
        SmsTemplate template = new SmsTemplate(req.getName(), req.getContent()); // match your actual constructor
        return repository.save(template);
    }

    public SmsTemplate update(String id, TemplateRequest req) {
        SmsTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SMS template not found."));
        template.setName(req.getName());
        template.setContent(req.getContent());
        return repository.save(template);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}

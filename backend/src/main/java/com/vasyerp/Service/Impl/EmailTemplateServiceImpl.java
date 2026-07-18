package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.EmailTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.TemplateRequest;
import com.vasyerp.Repository.EmailTemplateRepository;
import com.vasyerp.Service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailTemplateServiceImpl implements TemplateService<EmailTemplate>  {
    private final EmailTemplateRepository repository;

    public EmailTemplateServiceImpl(EmailTemplateRepository repository) {
        this.repository = repository;
    }

    public Page<EmailTemplate> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size));
    }

    public List<EmailTemplate> getAll() {
        return repository.findAll();
    }

    public EmailTemplate create(TemplateRequest req) {
        EmailTemplate template = new EmailTemplate(req.getName(), req.getContent()); // match your actual constructor
        return repository.save(template);
    }

    public EmailTemplate update(String id, TemplateRequest req) {
        EmailTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SMS template not found."));
        template.setName(req.getName());
        template.setContent(req.getContent());
        return repository.save(template);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}

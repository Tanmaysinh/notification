package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.PushTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.TemplateRequest;
import com.vasyerp.Repository.PushTemplateRepository;
import com.vasyerp.Service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PushTemplateServiceImpl implements TemplateService<PushTemplate>  {
    private final PushTemplateRepository repository;

    public PushTemplateServiceImpl(PushTemplateRepository repository) {
        this.repository = repository;
    }

    public Page<PushTemplate> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size));
    }

    public List<PushTemplate> getAll() {
        return repository.findAll();
    }

    public PushTemplate create(TemplateRequest req) {
        PushTemplate template = new PushTemplate(req.getName(), req.getContent()); // match your actual constructor
        return repository.save(template);
    }

    public PushTemplate update(String id, TemplateRequest req) {
        PushTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SMS template not found."));
        template.setName(req.getName());
        template.setContent(req.getContent());
        return repository.save(template);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}

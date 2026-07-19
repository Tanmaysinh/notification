package com.vasyerp.Controller;

import com.vasyerp.Entity.Contact;
import com.vasyerp.Model.ContactRequest;
import com.vasyerp.Model.PageRequestDto;
import com.vasyerp.Model.PageResultDto;
import com.vasyerp.Service.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/list")
    public PageResultDto<Contact> list(@RequestBody PageRequestDto request) {
        Page<Contact> page = contactService.list(request.getSearch(), request.getPage(), request.getSize());
        return PageResultDto.from(page);
    }

    @PostMapping
    public Contact create(@RequestBody ContactRequest request) {
        return contactService.create(request);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable String id, @RequestBody ContactRequest request) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable String id) {
        contactService.delete(id);
        return Map.of("deleted", true);
    }
}
package com.vasyerp.Service.Impl;


import com.vasyerp.Entity.Contact;
import com.vasyerp.Model.ContactRequest;
import com.vasyerp.Repository.ContactRepository;
import com.vasyerp.Service.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Page<Contact> list(String search, int page, int size) {
        return contactRepository.search(search, PageRequest.of(page, size));
    }

    public Contact create(ContactRequest req) {
        Contact contact = new Contact(req.getName(), req.getEmail(), req.getPhoneNumber(),req.getDeviceToken());
        return contactRepository.save(contact);
    }

    public Contact update(String id, ContactRequest req) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));
        contact.setName(req.getName());
        contact.setEmail(req.getEmail());
        contact.setPhoneNumber(req.getPhoneNumber());
        contact.setDeviceToken(req.getDeviceToken());
        return contactRepository.save(contact);
    }

    public void delete(String id) {
        contactRepository.deleteById(id);
    }
}
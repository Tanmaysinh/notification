package com.vasyerp.Service;

import com.vasyerp.Entity.Contact;
import com.vasyerp.Model.ContactRequest;
import org.springframework.data.domain.Page;

public interface ContactService {
    Page<Contact> list(String search, int page, int size);
    Contact create(ContactRequest req);
    Contact update(String id, ContactRequest req);
    void delete(String id);
}

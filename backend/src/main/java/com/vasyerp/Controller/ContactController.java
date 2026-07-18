package com.vasyerp.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.Contact;
import com.vasyerp.Model.ContactRequest;
import com.vasyerp.Model.EncryptedEnvelope;
import com.vasyerp.Model.PageRequestDto;
import com.vasyerp.Model.PageResultDto;
import com.vasyerp.Service.ContactService;
import com.vasyerp.crypto.AesGcmUtil;
import com.vasyerp.crypto.SessionKeyStore;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final SessionKeyStore sessionKeyStore;
    private final ContactService contactService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContactController(SessionKeyStore sessionKeyStore, ContactService contactService) {
        this.sessionKeyStore = sessionKeyStore;
        this.contactService = contactService;
    }

    @PostMapping("/list")
    public EncryptedEnvelope list(
            @RequestBody EncryptedEnvelope envelope,
            @RequestHeader("X-Session-Id") String sessionId
    ) throws Exception {
        byte[] aesKey = sessionKeyStore.get(sessionId);

        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
        PageRequestDto request = objectMapper.readValue(plaintext, PageRequestDto.class);

        Page<Contact> page = contactService.list(request.getSearch(), request.getPage(), request.getSize());

        byte[] responseBytes = objectMapper.writeValueAsBytes(PageResultDto.from(page));
        return AesGcmUtil.encrypt(aesKey, responseBytes);
    }

    @PostMapping
    public EncryptedEnvelope create(
            @RequestBody EncryptedEnvelope envelope,
            @RequestHeader("X-Session-Id") String sessionId
    ) throws Exception {
        byte[] aesKey = sessionKeyStore.get(sessionId);

        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
        ContactRequest request = objectMapper.readValue(plaintext, ContactRequest.class);

        Contact saved = contactService.create(request);

        byte[] responseBytes = objectMapper.writeValueAsBytes(saved);
        return AesGcmUtil.encrypt(aesKey, responseBytes);
    }

    @PutMapping("/{id}")
    public EncryptedEnvelope update(
            @PathVariable String id,
            @RequestBody EncryptedEnvelope envelope,
            @RequestHeader("X-Session-Id") String sessionId
    ) throws Exception {
        byte[] aesKey = sessionKeyStore.get(sessionId);

        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
        ContactRequest request = objectMapper.readValue(plaintext, ContactRequest.class);

        Contact saved = contactService.update(id, request);

        byte[] responseBytes = objectMapper.writeValueAsBytes(saved);
        return AesGcmUtil.encrypt(aesKey, responseBytes);
    }

    @DeleteMapping("/{id}")
    public EncryptedEnvelope delete(
            @PathVariable String id,
            @RequestHeader("X-Session-Id") String sessionId
    ) throws Exception {
        byte[] aesKey = sessionKeyStore.get(sessionId);

        contactService.delete(id);

        byte[] responseBytes = objectMapper.writeValueAsBytes(Map.of("deleted", true));
        return AesGcmUtil.encrypt(aesKey, responseBytes);
    }
}
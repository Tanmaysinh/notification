//package com.vasyerp.Controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Entity.NotificationTemplate;
//import com.vasyerp.Entity.PushTemplate;
//import com.vasyerp.Entity.SmsTemplate;
//import com.vasyerp.Model.EncryptedEnvelope;
//import com.vasyerp.Model.PageRequestDto;
//import com.vasyerp.Model.PageResultDto;
//import com.vasyerp.Model.TemplateRequest;
//import com.vasyerp.Service.TemplateService;
//import com.vasyerp.crypto.AesGcmUtil;
//import com.vasyerp.crypto.SessionKeyStore;
//import org.springframework.data.domain.Page;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/templates/sms")
//public class SmsTemplateController {
//
//    private final SessionKeyStore sessionKeyStore;
//    private final TemplateService<SmsTemplate> templateService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public SmsTemplateController(SessionKeyStore sessionKeyStore, TemplateService<SmsTemplate> templateService) {
//        this.sessionKeyStore = sessionKeyStore;
//        this.templateService = templateService;
//    }
//
//    @PostMapping("/list")
//    public EncryptedEnvelope list(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        PageRequestDto request = objectMapper.readValue(plaintext, PageRequestDto.class);
//
//        Page<SmsTemplate> page = templateService.list(request.getSearch(), request.getPage(), request.getSize());
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(PageResultDto.from(page));
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//    @PostMapping
//    public EncryptedEnvelope create(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        TemplateRequest request = objectMapper.readValue(plaintext, TemplateRequest.class);
//
//        NotificationTemplate saved = templateService.create(request);
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(saved);
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//    @PutMapping("/{id}")
//    public EncryptedEnvelope update(
//            @PathVariable String id,
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        TemplateRequest request = objectMapper.readValue(plaintext, TemplateRequest.class);
//
//        NotificationTemplate saved = templateService.update(id, request);
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(saved);
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//    @DeleteMapping("/{id}")
//    public EncryptedEnvelope delete(
//            @PathVariable String id,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        templateService.delete(id);
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(Map.of("deleted", true));
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//
//    @GetMapping("/")
//    public EncryptedEnvelope list1(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        PageRequestDto request = objectMapper.readValue(plaintext, PageRequestDto.class);
//
//        Page<SmsTemplate> page = templateService.list(request.getSearch(), request.getPage(), request.getSize());
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(PageResultDto.from(page));
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//
//}


package com.vasyerp.Controller;

import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.SmsTemplate;
import com.vasyerp.Model.PageRequestDto;
import com.vasyerp.Model.TemplateRequest;
import com.vasyerp.Service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates/sms")
public class SmsTemplateController {

    private final TemplateService<SmsTemplate> templateService;

    public SmsTemplateController(TemplateService<SmsTemplate> templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/list")
    public Page<SmsTemplate> list(@RequestBody PageRequestDto request) {
        return templateService.list(
                request.getSearch(),
                request.getPage(),
                request.getSize()
        );
    }

    @PostMapping
    public NotificationTemplate create(@RequestBody TemplateRequest request) {
        return templateService.create(request);
    }

    @PutMapping("/{id}")
    public NotificationTemplate update(
            @PathVariable String id,
            @RequestBody TemplateRequest request
    ) {
        return templateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable String id) {
        templateService.delete(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/all")
    public List<SmsTemplate> list1() {
        return templateService.getAll();
    }
}
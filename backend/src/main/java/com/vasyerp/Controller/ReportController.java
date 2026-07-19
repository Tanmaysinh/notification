//package com.vasyerp.Controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Model.EncryptedEnvelope;
//import com.vasyerp.Model.ReportFilterRequest;
//import com.vasyerp.Model.ReportPage;
//import com.vasyerp.Model.RetryRequest;
//import com.vasyerp.Service.ReportService;
//import com.vasyerp.crypto.AesGcmUtil;
//import com.vasyerp.crypto.SessionKeyStore;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/report")
//public class ReportController {
//
//    private final SessionKeyStore sessionKeyStore;
//    private final ReportService reportService;
//    private final ObjectMapper objectMapper ;
//
//    public ReportController(SessionKeyStore sessionKeyStore, ReportService reportService, ObjectMapper objectMapper) {
//        this.sessionKeyStore = sessionKeyStore;
//        this.reportService = reportService;
//        this.objectMapper = objectMapper;
//    }
//
//    @PostMapping("/list")
//    public EncryptedEnvelope list(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        ReportFilterRequest filter = objectMapper.readValue(plaintext, ReportFilterRequest.class);
//
//        ReportPage result = reportService.getReport(filter);
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(result);
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//    @PostMapping("/retry")
//    public EncryptedEnvelope retry(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        RetryRequest req = objectMapper.readValue(plaintext, RetryRequest.class);
//
//        reportService.retry(req.getRequestId(), req.getContactId());
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(Map.of("retried", true));
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//}



package com.vasyerp.Controller;

import com.vasyerp.Model.ReportFilterRequest;
import com.vasyerp.Model.ReportPage;
import com.vasyerp.Model.RetryRequest;
import com.vasyerp.Service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/list")
    public ReportPage list(@RequestBody ReportFilterRequest filter) {
        return reportService.getReport(filter);
    }

    @PostMapping("/retry")
    public Map<String, Boolean> retry(@RequestBody RetryRequest request) {
        reportService.retry(request.getRequestId(), request.getContactId(),request.getChannelType());
        return Map.of("retried", true);
    }
}
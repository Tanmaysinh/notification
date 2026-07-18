//package com.vasyerp.Controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Model.EncryptedEnvelope;
//import com.vasyerp.Model.SendNotificationRequest;
//import com.vasyerp.Model.SendNotificationResponse;
//import com.vasyerp.Service.NotificationService;
//import com.vasyerp.crypto.AesGcmUtil;
//import com.vasyerp.crypto.SessionKeyStore;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Arrays;
//
//@RestController
//@RequestMapping("/api/notifications")
//public class NotificationController {
//
//    private final SessionKeyStore sessionKeyStore;
//    private final NotificationService notificationService;
//    private final ObjectMapper objectMapper;
//
//    public NotificationController(SessionKeyStore sessionKeyStore, NotificationService notificationService, ObjectMapper objectMapper) {
//        this.sessionKeyStore = sessionKeyStore;
//        this.notificationService = notificationService;
//        this.objectMapper = objectMapper;
//    }
//
//    @PostMapping("/send")
//    public EncryptedEnvelope send(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        System.out.println(new String(plaintext));
//        SendNotificationRequest request = objectMapper.readValue(plaintext, SendNotificationRequest.class);
//
//        String requestId = notificationService.send(request);
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(new SendNotificationResponse(requestId));
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//}


package com.vasyerp.Controller;

import com.vasyerp.Model.SendNotificationRequest;
import com.vasyerp.Model.SendNotificationResponse;
import com.vasyerp.Service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public SendNotificationResponse send(@RequestBody SendNotificationRequest request) {
        String requestId = notificationService.send(request);
        return new SendNotificationResponse(requestId);
    }
}
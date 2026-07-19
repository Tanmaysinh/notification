//package com.vasyerp.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Entity.NotificationTemplate;
//import com.vasyerp.Entity.RequestStatus;
//import com.vasyerp.Model.ContactDto;
//import com.vasyerp.Model.ContactStatusMessage;
//import com.vasyerp.Model.NotificationRequestModel;
//import com.vasyerp.Repository.RequestStatusRepository;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.Collectors;
//
//@Component
//public class NotificationListener {
//
//    private final ThreadPoolTaskExecutor contactProcessorExecutor;
//    private final RabbitTemplate rabbitTemplate;
//    private final RequestStatusRepository RequestStatusRepository;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${app.rabbitmq.status-exchange}")
//    private String statusExchange;
//
//    @Value("${app.rabbitmq.status-routing-key}")
//    private String statusRoutingKey;
//
//    public NotificationListener(
//            ThreadPoolTaskExecutor contactProcessorExecutor,
//            RabbitTemplate rabbitTemplate,
//            RequestStatusRepository RequestStatusRepository
//    ) {
//        this.contactProcessorExecutor = contactProcessorExecutor;
//        this.rabbitTemplate = rabbitTemplate;
//        this.RequestStatusRepository = RequestStatusRepository;
//    }
//
//    @RabbitListener(queues = "${app.rabbitmq.notification-queue}", containerFactory = "rabbitListenerContainerFactory")
//    public void onNotificationRequest(NotificationRequestModel request) {
//        String requestId = request.getRequestId();
//        Map<String, NotificationTemplate> templates = request.getTemplates();
//        List<ContactDto> contacts = request.getContacts();
//
//        // Thread-safe map to accumulate per-contact results as futures complete
//        Map<String, ContactResult> resultsByContact = new ConcurrentHashMap<>();
//
//        List<CompletableFuture<Void>> futures = contacts.stream()
//                .map(contact -> CompletableFuture.runAsync(
//                        () -> processContact(requestId, contact, templates, resultsByContact),
//                        contactProcessorExecutor
//                ))
//                .collect(Collectors.toList());
//
//        // Block this listener thread (one of the rabbitExecutor pool threads) until
//        // every contact for this request has been processed — this keeps each
//        // request's completion self-contained before writing the final status row
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        saveFinalStatus(requestId, templates, resultsByContact);
//    }
//
//    private void processContact(
//            String requestId,
//            ContactDto contact,
//            Map<String, NotificationTemplate> templates,
//            Map<String, ContactResult> resultsByContact
//    ) {
//        // Simulate actual SMS/email/push dispatch per channel present in templates.
//        // Replace this block with real provider calls (Twilio, SES, FCM, etc.) later.
//        boolean success = true;
//        try {
//            for (String channel : templates.keySet()) {
//                // simulated per-channel send — real implementation calls the provider here
//            }
//        } catch (Exception e) {
//            success = false;
//        }
//
//        String status = success ? "SENT" : "FAILED";
//        resultsByContact.put(contact.getContactId(), new ContactResult(status, 0));
//
//        // Publish a status update onto the separate status queue, with a random
//        // delay before publishing — simulates async confirmation arriving from
//        // a provider webhook rather than an immediate result. Runs on the same
//        // contactProcessorExecutor thread, not blocking the listener thread pool.
//        try {
//            long randomDelayMillis = ThreadLocalRandom.current().nextInt(500, 3000);
//            Thread.sleep(randomDelayMillis);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        ContactStatusMessage statusMessage = new ContactStatusMessage(requestId, contact.getContactId(), status);
//        rabbitTemplate.convertAndSend(statusExchange, statusRoutingKey, statusMessage);
//    }
//
//    private void saveFinalStatus(
//            String requestId,
//            Map<String, NotificationTemplate> templates,
//            Map<String, ContactResult> resultsByContact
//    ) {
//        try {
////            String templatesJson = objectMapper.writeValueAsString(templates);
//            String statusJson = objectMapper.writeValueAsString(resultsByContact);
//
//            RequestStatus statusMaster = new RequestStatus(requestId, statusJson);
//            RequestStatusRepository.save(statusMaster);
//        } catch (Exception e) {
//            // Log this in a real setup — losing the final status write means the
//            // report page won't reflect this request's outcome
//            e.printStackTrace();
//        }
//    }
//
//    /** Shape matching {"status": [...], "retryCount": n} per your spec. */
//    public static class ContactResult {
//        private List<String> status;
//        private int retryCount;
//
//        public ContactResult() {}
//
//        public ContactResult(String initialStatus, int retryCount) {
//            this.status = List.of(initialStatus);
//            this.retryCount = retryCount;
//        }
//
//        public List<String> getStatus() { return status; }
//        public void setStatus(List<String> status) { this.status = status; }
//        public int getRetryCount() { return retryCount; }
//        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
//    }
//}



package com.vasyerp.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.*;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Repository.RequestStatusRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class NotificationListener {

    private final ThreadPoolTaskExecutor contactProcessorExecutor;
    private final RabbitTemplate rabbitTemplate;
    private final RequestStatusRepository requestStatusRepository;
    private final NotificationRequestRepository notificationRequestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.rabbitmq.status-exchange}")
    private String statusExchange;

    @Value("${app.rabbitmq.status-routing-key}")
    private String statusRoutingKey;

    public NotificationListener(
            ThreadPoolTaskExecutor contactProcessorExecutor,
            RabbitTemplate rabbitTemplate,
            RequestStatusRepository requestStatusRepository,
            NotificationRequestRepository notificationRequestRepository
    ) {
        this.contactProcessorExecutor = contactProcessorExecutor;
        this.rabbitTemplate = rabbitTemplate;
        this.requestStatusRepository = requestStatusRepository;
        this.notificationRequestRepository = notificationRequestRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.notification-queue}", containerFactory = "rabbitListenerContainerFactory")
    public void onNotificationRequest(NotificationRequestModel request) {
        String requestId = request.getRequestId();
        Map<String, NotificationTemplateModel> templates = request.getTemplates();
        List<ContactDto> contacts = request.getContacts();

        notificationRequestRepository.findById(requestId).ifPresent(r -> {
            r.setStatus("PROCESSING");
            notificationRequestRepository.save(r);
        });

        List<CompletableFuture<Void>> futures = contacts.stream()
                .map(contact -> CompletableFuture.runAsync(
                        () -> processContact(requestId, contact, templates),
                        contactProcessorExecutor
                ))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        notificationRequestRepository.findById(requestId).ifPresent(r -> {
            r.setStatus("COMPLETED");
            notificationRequestRepository.save(r);
        });
    }

    private void processContact(String requestId, ContactDto contact, Map<String, NotificationTemplateModel> templates) {
        for (String channel : templates.keySet()) {
            // Simulated immediate send — replace with real Twilio/SES/FCM call
//            updateChannelStatus(requestId, contact.getContactId(), channel, "SENT");

            try {
                long randomDelayMillis = ThreadLocalRandom.current().nextInt(500, 3000);
                Thread.sleep(randomDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String finalStatus = ThreadLocalRandom.current().nextBoolean() ? "DELIVERED" : "FAILED";
            rabbitTemplate.convertAndSend(
                    statusExchange, statusRoutingKey,
                    new ContactStatusMessage(requestId, contact.getContactId(), channel, finalStatus)
            );
        }
    }

    private void updateChannelStatus(String requestId, String contactId, String channelType, String newStatus) {
        RequestStatus rs = requestStatusRepository.findByRequestRequestIdAndContactId(requestId, contactId).orElse(null);
        if (rs == null) return;

        try {
            Map<String, ChannelStatusEntry> map = objectMapper.readValue(
                    rs.getChannelWiseStatusJson(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, ChannelStatusEntry.class)
            );

            ChannelStatusEntry entry = map.get(channelType);
            if (entry == null) return;

            var history = new java.util.ArrayList<>(entry.getStatus());
            history.add(newStatus);
            entry.setStatus(history);
            map.put(channelType, entry);

            rs.setChannelWiseStatusJson(objectMapper.writeValueAsString(map));
            requestStatusRepository.save(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static class ContactResult {
        private List<String> status;
        private int retryCount;

        public ContactResult() {}

        public ContactResult(String initialStatus, int retryCount) {
            this.status = List.of(initialStatus);
            this.retryCount = retryCount;
        }

        public List<String> getStatus() { return status; }
        public void setStatus(List<String> status) { this.status = status; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    }
}
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
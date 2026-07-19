package com.vasyerp.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.ChannelStatusEntry;
import com.vasyerp.Model.ContactStatusMessage;
import com.vasyerp.Repository.RequestStatusRepository;
import com.vasyerp.Service.CacheService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StatusListener {

    private final RequestStatusRepository requestStatusRepository;
    private final CacheService cacheService;
    private final RabbitTemplate rabbitTemplate;
    private final ThreadPoolTaskExecutor statusPublisherExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.rabbitmq.status-exchange}")
    private String statusExchange;

    @Value("${app.rabbitmq.status-routing-key}")
    private String statusRoutingKey;

    public StatusListener(
            RequestStatusRepository requestStatusRepository,
            CacheService cacheService,
            RabbitTemplate rabbitTemplate,
            ThreadPoolTaskExecutor statusPublisherExecutor
    ) {
        this.requestStatusRepository = requestStatusRepository;
        this.cacheService = cacheService;
        this.rabbitTemplate = rabbitTemplate;
        this.statusPublisherExecutor = statusPublisherExecutor;
    }

    @RabbitListener(queues = "${app.rabbitmq.status-queue}")
    public void onStatusUpdate(ContactStatusMessage msg) {
        System.out.println(msg);

        RequestStatus rs = requestStatusRepository
                .findByRequestRequestIdAndContactId(msg.getRequestId(), msg.getContactId())
                .orElse(null);
        if (rs == null) return;

        try {
            Map<String, ChannelStatusEntry> map = objectMapper.readValue(
                    rs.getChannelWiseStatusJson(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, ChannelStatusEntry.class)
            );

            ChannelStatusEntry entry = map.get(msg.getChannelType());
            if (entry == null) return;

//            var history = new ArrayList<>(entry.getStatus());
//            history.add(msg.getStatus());
//            entry.setStatus(history);

            if ("DELIVERED".equalsIgnoreCase(msg.getStatus())
                    || "FAILED".equalsIgnoreCase(msg.getStatus())) {

                var history = new ArrayList<>(entry.getStatus());
                history.add (entry.getRetryCount(),msg.getStatus());
                entry.setStatus(history);
            }

            if ("FAILED".equalsIgnoreCase(msg.getStatus())) {
                int maxRetries = cacheService.getMaxRetries(msg.getRequestId());
                if (entry.getRetryCount() < maxRetries) {
                    entry.setRetryCount(entry.getRetryCount() + 1);
                    scheduleRetryPublish(msg.getRequestId(), msg.getContactId(), msg.getChannelType());
                }
            }

            map.put(msg.getChannelType(), entry);
            rs.setChannelWiseStatusJson(objectMapper.writeValueAsString(map));
            requestStatusRepository.save(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleRetryPublish(String requestId, String contactId, String channelType) {
        statusPublisherExecutor.execute(() -> {
            try {
                long delay = ThreadLocalRandom.current().nextInt(500, 3000);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String status = ThreadLocalRandom.current().nextBoolean() ? "DELIVERED" : "FAILED";
            rabbitTemplate.convertAndSend(
                    statusExchange, statusRoutingKey,
                    new ContactStatusMessage(requestId, contactId, channelType, status)
            );
        });
    }
}
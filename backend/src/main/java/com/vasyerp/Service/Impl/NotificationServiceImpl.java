package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.Campaign;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Model.SendNotificationRequest;
import com.vasyerp.Model.ContactDto;
import com.vasyerp.Model.NotificationRequestModel;
import com.vasyerp.Repository.CampaignRepository;
import com.vasyerp.Repository.ContactRepository;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Repository.SmsTemplateRepository;
import com.vasyerp.Repository.EmailTemplateRepository;
import com.vasyerp.Repository.PushTemplateRepository;
import com.vasyerp.Entity.Contact;
import com.vasyerp.Service.NotificationService;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final CampaignRepository campaignRepository;
    private final ContactRepository contactRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final PushTemplateRepository pushTemplateRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.notification-exchange}")
    private String notificationExchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public NotificationServiceImpl(
            NotificationRequestRepository notificationRequestRepository,
            CampaignRepository campaignRepository,
            ContactRepository contactRepository,
            SmsTemplateRepository smsTemplateRepository,
            EmailTemplateRepository emailTemplateRepository,
            PushTemplateRepository pushTemplateRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.campaignRepository = campaignRepository;
        this.contactRepository = contactRepository;
        this.smsTemplateRepository = smsTemplateRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.pushTemplateRepository = pushTemplateRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String send(SendNotificationRequest req) {
        if (req.getContactIds() == null || req.getContactIds().isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required.");
        }

        String smsTemplateId = req.getSmsTemplateId();
        String emailTemplateId = req.getEmailTemplateId();
        String pushTemplateId = req.getPushTemplateId();

        if (req.getCampaignId() != null) {
            Campaign campaign = campaignRepository.findById(req.getCampaignId())
                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
            smsTemplateId = campaign.getSmsTemplateId();
            emailTemplateId = campaign.getEmailTemplateId();
            pushTemplateId = campaign.getPushTemplateId();
        }

        if (smsTemplateId == null && emailTemplateId == null && pushTemplateId == null) {
            throw new IllegalArgumentException("At least one channel must be configured.");
        }

        // Resolve actual template content once, up front, rather than re-fetching per contact later
        Map<String, String> templates = new HashMap<>();
        if (smsTemplateId != null) {
            smsTemplateRepository.findById(smsTemplateId).ifPresent(t -> templates.put("sms", t.getContent()));
        }
        if (emailTemplateId != null) {
            emailTemplateRepository.findById(emailTemplateId).ifPresent(t -> templates.put("email", t.getContent()));
        }
        if (pushTemplateId != null) {
            pushTemplateRepository.findById(pushTemplateId).ifPresent(t -> templates.put("push", t.getContent()));
        }

        // Persist the request row immediately, before publishing — this row is
        // the source of truth even if the broker is briefly unavailable
        NotificationRequest request = new NotificationRequest(
                req.getCampaignId(), smsTemplateId, emailTemplateId, pushTemplateId,
                req.getContactIds().size(), req.getScheduleTime()
        );
        NotificationRequest saved = notificationRequestRepository.save(request);

        List<Contact> contacts = contactRepository.findAllById(req.getContactIds());
        List<ContactDto> contactDtos = contacts.stream()
                .map(c -> new ContactDto(c.getContactId(), c.getName(), c.getEmail(), c.getPhoneNumber()))
                .collect(Collectors.toList());

        NotificationRequestModel mqModel = new NotificationRequestModel(
                saved.getRequestId(), templates, contactDtos
        );

        long delayMillis = 0;
        if (req.getScheduleTime() != null && req.getScheduleTime().isAfter(Instant.now())) {
            delayMillis = Duration.between(Instant.now(), req.getScheduleTime()).toMillis();
        }
        final long finalDelay = delayMillis;

        MessagePostProcessor delayProcessor = message -> {
            message.getMessageProperties().setHeader("x-delay", finalDelay);
            return message;
        };

        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, mqModel, delayProcessor);

        return saved.getRequestId();
    }
}
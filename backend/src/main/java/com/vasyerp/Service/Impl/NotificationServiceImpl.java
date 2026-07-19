//package com.vasyerp.Service.Impl;
//
//import com.vasyerp.Entity.Campaign;
//import com.vasyerp.Entity.NotificationRequest;
//import com.vasyerp.Entity.NotificationTemplate;
//import com.vasyerp.Model.SendNotificationRequest;
//import com.vasyerp.Model.ContactDto;
//import com.vasyerp.Model.NotificationRequestModel;
//import com.vasyerp.Repository.CampaignRepository;
//import com.vasyerp.Repository.ContactRepository;
//import com.vasyerp.Repository.NotificationRequestRepository;
//import com.vasyerp.Repository.SmsTemplateRepository;
//import com.vasyerp.Repository.EmailTemplateRepository;
//import com.vasyerp.Repository.PushTemplateRepository;
//import com.vasyerp.Entity.Contact;
//import com.vasyerp.Service.NotificationService;
//import org.springframework.amqp.core.MessagePostProcessor;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class NotificationServiceImpl implements NotificationService {
//
//    private final NotificationRequestRepository notificationRequestRepository;
//    private final CampaignRepository campaignRepository;
//    private final ContactRepository contactRepository;
//    private final SmsTemplateRepository smsTemplateRepository;
//    private final EmailTemplateRepository emailTemplateRepository;
//    private final PushTemplateRepository pushTemplateRepository;
//    private final RabbitTemplate rabbitTemplate;
//
//    @Value("${app.rabbitmq.notification-exchange}")
//    private String notificationExchange;
//
//    @Value("${app.rabbitmq.notification-routing-key}")
//    private String notificationRoutingKey;
//
//    public NotificationServiceImpl(
//            NotificationRequestRepository notificationRequestRepository,
//            CampaignRepository campaignRepository,
//            ContactRepository contactRepository,
//            SmsTemplateRepository smsTemplateRepository,
//            EmailTemplateRepository emailTemplateRepository,
//            PushTemplateRepository pushTemplateRepository,
//            RabbitTemplate rabbitTemplate
//    ) {
//        this.notificationRequestRepository = notificationRequestRepository;
//        this.campaignRepository = campaignRepository;
//        this.contactRepository = contactRepository;
//        this.smsTemplateRepository = smsTemplateRepository;
//        this.emailTemplateRepository = emailTemplateRepository;
//        this.pushTemplateRepository = pushTemplateRepository;
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    public String send(SendNotificationRequest req) {
//        if (req.getContactIds() == null || req.getContactIds().isEmpty()) {
//            throw new IllegalArgumentException("At least one recipient is required.");
//        }
//
//        String smsTemplateId = req.getSmsTemplateId();
//        String emailTemplateId = req.getEmailTemplateId();
//        String pushTemplateId = req.getPushTemplateId();
//
//        if (req.getCampaignId() != null) {
//            Campaign campaign = campaignRepository.findById(req.getCampaignId())
//                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
//            smsTemplateId = campaign.getSmsTemplateId();
//            emailTemplateId = campaign.getEmailTemplateId();
//            pushTemplateId = campaign.getPushTemplateId();
//        }
//
//        if (smsTemplateId == null && emailTemplateId == null && pushTemplateId == null) {
//            throw new IllegalArgumentException("At least one channel must be configured.");
//        }
//
//        // Resolve actual template content once, up front, rather than re-fetching per contact later
//        Map<String, NotificationTemplate> templates = new HashMap<>();
//        if (smsTemplateId != null) {
//            smsTemplateRepository.findById(smsTemplateId).ifPresent(t -> templates.put("sms", t));
//        }
//        if (emailTemplateId != null) {
//            emailTemplateRepository.findById(emailTemplateId).ifPresent(t -> templates.put("email", t));
//        }
//        if (pushTemplateId != null) {
//            pushTemplateRepository.findById(pushTemplateId).ifPresent(t -> templates.put("push", t));
//        }
//
//        // Persist the request row immediately, before publishing — this row is
//        // the source of truth even if the broker is briefly unavailable
//        NotificationRequest request = new NotificationRequest(
//                req.getCampaignId(), templates.get("sms"), templates.get("email"), templates.get("push"),
//                req.getContactIds().size(), req.getScheduleTime()
//        );
//        NotificationRequest saved = notificationRequestRepository.save(request);
//
//        List<Contact> contacts = contactRepository.findAllById(req.getContactIds());
//        List<ContactDto> contactDtos = contacts.stream()
//                .map(c -> new ContactDto(c.getContactId(), c.getName(), c.getEmail(), c.getPhoneNumber()))
//                .collect(Collectors.toList());
//
//        NotificationRequestModel mqModel = new NotificationRequestModel(
//                saved.getRequestId(), templates, contactDtos
//        );
//
//        long delayMillis = 0;
//        if (req.getScheduleTime() != null && req.getScheduleTime().isAfter(Instant.now())) {
//            delayMillis = Duration.between(Instant.now(), req.getScheduleTime()).toMillis();
//        }
//        final long finalDelay = delayMillis;
//
//        MessagePostProcessor delayProcessor = message -> {
//            message.getMessageProperties().setHeader("x-delay", finalDelay);
//            return message;
//        };
//
//        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, mqModel, delayProcessor);
//
//        return saved.getRequestId();
//    }
//}


package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.*;
import com.vasyerp.Model.ChannelStatusEntry;
import com.vasyerp.Model.NotificationTemplateModel;
import com.vasyerp.Model.SendNotificationRequest;
import com.vasyerp.Repository.*;
import com.vasyerp.Service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final CampaignRepository campaignRepository;
    private final ContactRepository contactRepository;
    private final SmsTemplateRepository smsTemplateRepository; // adjust if templates are unified into one repo
    private final EmailTemplateRepository emailTemplateRepository;
    private final PushTemplateRepository pushTemplateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationServiceImpl(
            NotificationRequestRepository notificationRequestRepository,
            RequestStatusRepository requestStatusRepository,
            CampaignRepository campaignRepository,
            ContactRepository contactRepository,
            SmsTemplateRepository smsTemplateRepository,
            EmailTemplateRepository emailTemplateRepository,
            PushTemplateRepository pushTemplateRepository
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.requestStatusRepository = requestStatusRepository;
        this.campaignRepository = campaignRepository;
        this.contactRepository = contactRepository;
        this.smsTemplateRepository = smsTemplateRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.pushTemplateRepository = pushTemplateRepository;
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

//        NotificationTemplate smsTemplate = smsTemplateId != null ? smsTemplateRepository.findById(smsTemplateId).orElse(null) : null;
//        NotificationTemplate emailTemplate = emailTemplateId != null ? emailTemplateRepository.findById(emailTemplateId).orElse(null) : null;
//        NotificationTemplate pushTemplate = pushTemplateId != null ? pushTemplateRepository.findById(pushTemplateId).orElse(null) : null;
//
//
        NotificationTemplateModel smsModel = smsTemplateId != null
                ? smsTemplateRepository.findById(smsTemplateId)
                .map(t -> new NotificationTemplateModel(t.getTemplateId(), t.getName(), t.getContent()))
                .orElse(null)
                : null;

        NotificationTemplateModel emailModel = emailTemplateId != null
                ? emailTemplateRepository.findById(emailTemplateId)
                .map(t -> new NotificationTemplateModel(t.getTemplateId(), t.getName(), t.getContent()))
                .orElse(null)
                : null;

        NotificationTemplateModel pushModel = pushTemplateId != null
                ? pushTemplateRepository.findById(pushTemplateId)
                .map(t -> new NotificationTemplateModel(t.getTemplateId(), t.getName(), t.getContent()))
                .orElse(null)
                : null;


        if (smsModel == null && emailModel == null && pushModel == null) {
            throw new IllegalArgumentException("At least one channel must be configured.");
        }

        NotificationRequest request = new NotificationRequest(
                req.getCampaignId(), smsModel, emailModel, pushModel,
                req.getContactIds().size(), req.getScheduleTime()
        );

//        NotificationRequest request = new NotificationRequest(
//                req.getCampaignId(), smsTemplate, emailTemplate, pushTemplate,
//                req.getContactIds().size(), req.getScheduleTime()
//        );
        NotificationRequest saved = notificationRequestRepository.save(request);

        List<Contact> contacts = contactRepository.findAllById(req.getContactIds());

        for (Contact contact : contacts) {
            Map<String, ChannelStatusEntry> channelMap = new HashMap<>();
            if (smsModel != null) {
                channelMap.put("sms", new ChannelStatusEntry("PENDING", 0, contact.getPhoneNumber()));
            }
            if (emailModel != null) {
                channelMap.put("email", new ChannelStatusEntry("PENDING", 0, contact.getEmail()));
            }
            if (pushModel != null) {
                channelMap.put("push", new ChannelStatusEntry("PENDING", 0, contact.getDeviceToken()));
            }

            try {
                String json = objectMapper.writeValueAsString(channelMap);
                requestStatusRepository.save(new RequestStatus(saved, contact.getContactId(), json));
            } catch (Exception e) {
                throw new RuntimeException("Could not initialize contact status.", e);
            }
        }

        // No MQ publish here anymore — the scheduler picks this up based on scheduleTime
        return saved.getRequestId();
    }
}
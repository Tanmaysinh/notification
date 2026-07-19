//package com.vasyerp.Component;
//
//import com.vasyerp.Entity.NotificationRequest;
//import com.vasyerp.Repository.NotificationRequestRepository;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//public class NotificationScheduler {
//
//    private final NotificationRequestRepository notificationRequestRepository;
//    private final RabbitTemplate rabbitTemplate;
//
//    @Value("${app.rabbitmq.notification-exchange}")
//    private String exchange;
//
//    @Value("${app.rabbitmq.notification-routing-key}")
//    private String routingKey;
//
//    public NotificationScheduler(NotificationRequestRepository notificationRequestRepository, RabbitTemplate rabbitTemplate) {
//        this.notificationRequestRepository = notificationRequestRepository;
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    @Scheduled(fixedDelay = 30000) // every 30 seconds
//    public void publishScheduledNotifications() {
//
//        List<NotificationRequest> notifications =
//                notificationRequestRepository.findByStatusAndScheduleTimeLessThanEqual(
//                        "PENDING",
//                        Instant.now());
//
//        for (NotificationRequest notification : notifications) {
//
//            rabbitTemplate.convertAndSend(
//                    exchange,
//                    routingKey,
//                    notification
//            );
//
//            notification.setStatus("QUEUED");
//            notificationRequestRepository.save(notification);
//        }
//    }
//}



package com.vasyerp.Component;

import com.vasyerp.Entity.Contact;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Entity.NotificationTemplate;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.ContactDto;
import com.vasyerp.Model.NotificationRequestModel;
import com.vasyerp.Model.NotificationTemplateModel;
import com.vasyerp.Repository.ContactRepository;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Repository.RequestStatusRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationScheduler {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final ContactRepository contactRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.notification-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String routingKey;

    public NotificationScheduler(
            NotificationRequestRepository notificationRequestRepository,
            RequestStatusRepository requestStatusRepository,
            ContactRepository contactRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.requestStatusRepository = requestStatusRepository;
        this.contactRepository = contactRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional(readOnly = true)
    public void publishScheduledNotifications() {
        List<NotificationRequest> due = notificationRequestRepository
                .findByStatusAndScheduleTimeLessThanEqual("SCHEDULED", Instant.now());

        for (NotificationRequest req : due) {
//            List<RequestStatus> statuses = requestStatusRepository.findByRequestId(req.getRequestId());
            List<RequestStatus> statuses=req.getRequestStatuses();
            List<String> contactIds = statuses.stream().map(RequestStatus::getContactId).collect(Collectors.toList());
            List<Contact> contacts = contactRepository.findAllById(contactIds);

            List<ContactDto> contactDtos = contacts.stream()
                    .map(c -> new ContactDto(c.getContactId(), c.getName(), c.getEmail(), c.getPhoneNumber()))
                    .collect(Collectors.toList());

            Map<String, NotificationTemplateModel> templates = new HashMap<>();
            if (req.getSmsTemplate() != null) templates.put("sms", req.getSmsTemplate());
            if (req.getEmailTemplate() != null) templates.put("email", req.getEmailTemplate());
            if (req.getPushTemplate() != null) templates.put("push", req.getPushTemplate());

            NotificationRequestModel model = new NotificationRequestModel(req.getRequestId(), templates, contactDtos);
            rabbitTemplate.convertAndSend(exchange, routingKey, model);

            req.setStatus("SENT");
            notificationRequestRepository.save(req);
        }
    }
}
package com.vasyerp.Service.Impl;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.Contact;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.ChannelStatus;
import com.vasyerp.Model.ReportFilterRequest;
import com.vasyerp.Model.ReportPage;
import com.vasyerp.Model.ReportRow;
import com.vasyerp.Repository.*;
import com.vasyerp.Service.ReportService;
import com.vasyerp.Component.NotificationListener.ContactResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
        import java.util.stream.Collectors;

import static com.vasyerp.Repository.NotificationRequestSpecifications.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final ContactRepository contactRepository;
    private final CampaignRepository campaignRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final PushTemplateRepository pushTemplateRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportServiceImpl(
            NotificationRequestRepository notificationRequestRepository,
            RequestStatusRepository requestStatusRepository,
            ContactRepository contactRepository, CampaignRepository campaignRepository, SmsTemplateRepository smsTemplateRepository, EmailTemplateRepository emailTemplateRepository, PushTemplateRepository pushTemplateRepository, RabbitTemplate rabbitTemplate
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.requestStatusRepository = requestStatusRepository;
        this.contactRepository = contactRepository;
        this.campaignRepository = campaignRepository;
        this.smsTemplateRepository = smsTemplateRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.pushTemplateRepository = pushTemplateRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${app.rabbitmq.notification-exchange}")
    private String notificationExchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public ReportPage getReport(ReportFilterRequest filter) {
        System.out.println(filter);
//        List<NotificationRequest> requests = notificationRequestRepository.findFiltered(
//                blankToNull(filter.getRequestId()),
//                blankToNull(filter.getCampaignId()),
//                blankToNull(filter.getRequestStatus()),
//                filter.getDateFrom(),
//                filter.getDateTo(),
//                blankToNull(filter.getNotificationType())
//        );


//        Specification<NotificationRequest> spec = Specification
//                .allOf(requestIdEquals(filter.getRequestId()))
//                .and(campaignIdEquals(filter.getCampaignId()))
//                .and(statusEquals(filter.getRequestStatus()))
//                .and(createdAfter(filter.getDateFrom()))
//                .and(createdBefore(filter.getDateTo()))
//                .and(notificationTypeMatches(filter.getNotificationType()));

        Specification<NotificationRequest> spec = Specification.allOf(
                requestIdEquals(filter.getRequestId()),
                campaignIdEquals(filter.getCampaignId()),
                statusEquals(filter.getRequestStatus()),
                createdAfter(filter.getDateFrom()),
                createdBefore(filter.getDateTo()),
                notificationTypeMatches(filter.getNotificationType())
        );
        List<NotificationRequest> requests = notificationRequestRepository.findAll(
                spec,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        System.out.println(requests);

        List<ReportRow> allRows = new ArrayList<>();

        for (NotificationRequest req : requests) {
            RequestStatus statusEntity = requestStatusRepository.findById(req.getRequestId()).orElse(null);
            if (statusEntity == null) continue;

            Map<String, String> templates = readTemplates(statusEntity.getTemplatesJson());
            // NEW shape: contactId -> channelType -> ChannelStatus
            Map<String, Map<String, ChannelStatus>> statusByContact = readNestedStatusMap(statusEntity.getStatusJson());

            if (statusByContact.isEmpty()) continue;

            Set<String> contactIds = statusByContact.keySet();
            Map<String, Contact> contactsById = contactRepository.findAllById(contactIds).stream()
                    .collect(Collectors.toMap(Contact::getContactId, c -> c));

            String campaignName = null;
            if (req.getCampaignId() != null) {
                campaignName = campaignRepository.findById(req.getCampaignId())
                        .map(c -> c.getName())
                        .orElse(null);
            }

            for (Map.Entry<String, Map<String, ChannelStatus>> contactEntry : statusByContact.entrySet()) {
                String contactId = contactEntry.getKey();
                Map<String, ChannelStatus> channelMap = contactEntry.getValue();
                Contact contact = contactsById.get(contactId);

                ReportRow row = new ReportRow();
                row.setRequestId(req.getRequestId());
                row.setCampaignId(req.getCampaignId());
                row.setCampaignName(campaignName);
                row.setScheduleTime(req.getScheduleTime());
                row.setCreatedAt(req.getCreatedAt());
                row.setRequestStatus(req.getStatus());
                row.setContactId(contactId);
                row.setContactName(contact != null ? contact.getName() : "(deleted contact)");
                row.setContactEmail(contact != null ? contact.getEmail() : null);
                row.setContactPhone(contact != null ? contact.getPhoneNumber() : null);

                List<ReportRow.ChannelRow> channelRows = new ArrayList<>();
                for (Map.Entry<String, ChannelStatus> chEntry : channelMap.entrySet()) {
                    String channelType = chEntry.getKey();
                    ChannelStatus cs = chEntry.getValue();

                    List<String> history = cs.getStatus() == null ? List.of() : cs.getStatus();
                    String latest = history.isEmpty() ? "UNKNOWN" : history.get(history.size() - 1);

                    ReportRow.ChannelRow channelRow = new ReportRow.ChannelRow();
                    channelRow.setChannelType(channelType);
                    channelRow.setContent(templates.get(channelType));
                    channelRow.setStatusHistory(history);
                    channelRow.setLatestStatus(latest);
                    channelRow.setRetryCount(cs.getRetryCount());
                    channelRow.setRetryEligible("FAILED".equalsIgnoreCase(latest) && cs.getRetryCount() < 3);

                    channelRows.add(channelRow);
                }
                row.setChannels(channelRows);

                allRows.add(row);
            }
        }

        // Apply the two filters that can only be evaluated per-contact, in memory
        List<ReportRow> filtered = allRows.stream()
                .filter(r -> matchesNotificationStatus(r, filter.getNotificationStatus()))
                .filter(r -> matchesContactSearch(r, filter.getContactSearch()))
                .collect(Collectors.toList());

        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() <= 0 ? 10 : filter.getSize();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<ReportRow> pageContent = filtered.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) filtered.size() / size);

        return new ReportPage(pageContent, filtered.size(), totalPages, page, size);
    }


    private Map<String, Map<String, ChannelStatus>> readNestedStatusMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, ChannelStatus>>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private boolean matchesNotificationStatus(ReportRow row, String notificationStatus) {
        if (notificationStatus == null || notificationStatus.isBlank()) return true;
        return row.getChannels().stream()
                .anyMatch(c -> notificationStatus.equalsIgnoreCase(c.getLatestStatus()));
    }

    private boolean matchesContactSearch(ReportRow row, String search) {
        if (search == null || search.isBlank()) return true;
        String term = search.toLowerCase();
        return (row.getContactEmail() != null && row.getContactEmail().toLowerCase().contains(term))
                || (row.getContactPhone() != null && row.getContactPhone().toLowerCase().contains(term));
    }

    private Map<String, String> readTemplates(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private Map<String, ContactResult> readStatusMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, ContactResult>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public void retry(String requestId, String contactId) {
        RequestStatus statusEntity = requestStatusRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        Map<String, ContactResult> statusByContact = readStatusMap(statusEntity.getStatusJson());
        ContactResult result = statusByContact.get(contactId);

        if (result == null) {
            throw new IllegalArgumentException("Contact not found in this request.");
        }

        String latestStatus = result.getStatus().isEmpty() ? "UNKNOWN" : result.getStatus().get(result.getStatus().size() - 1);
        if (!"FAILED".equalsIgnoreCase(latestStatus)) {
            throw new IllegalArgumentException("Only failed notifications can be retried.");
        }
        if (result.getRetryCount() >= 3) {
            throw new IllegalArgumentException("Retry limit reached for this contact.");
        }

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact no longer exists."));

        Map<String, String> templates = readTemplates(statusEntity.getTemplatesJson());

        // Mark as PENDING immediately and increment retryCount optimistically —
        // avoids double-retry if the user clicks twice before the async result lands
        List<String> updatedHistory = new ArrayList<>(result.getStatus());
        updatedHistory.add("PENDING");
        result.setStatus(updatedHistory);
        result.setRetryCount(result.getRetryCount() + 1);
        statusByContact.put(contactId, result);

        try {
            statusEntity.setStatusJson(objectMapper.writeValueAsString(statusByContact));
            requestStatusRepository.save(statusEntity);
        } catch (Exception e) {
            throw new RuntimeException("Could not update retry status.", e);
        }

        // Re-publish just this one contact for reprocessing — sent immediately,
        // no schedule delay, since a retry is by definition already overdue
        com.vasyerp.Model.ContactDto contactDto = new com.vasyerp.Model.ContactDto(
                contact.getContactId(), contact.getName(), contact.getEmail(), contact.getPhoneNumber()
        );
        com.vasyerp.Model.NotificationRequestModel retryModel = new com.vasyerp.Model.NotificationRequestModel(
                requestId, templates, List.of(contactDto)
        );

        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, retryModel);
    }


}
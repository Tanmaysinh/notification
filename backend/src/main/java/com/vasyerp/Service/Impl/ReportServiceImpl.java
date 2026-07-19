//package com.vasyerp.Service.Impl;
//
//
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Entity.Contact;
//import com.vasyerp.Entity.NotificationRequest;
//import com.vasyerp.Entity.RequestStatus;
//import com.vasyerp.Model.ChannelStatus;
//import com.vasyerp.Model.ReportFilterRequest;
//import com.vasyerp.Model.ReportPage;
//import com.vasyerp.Model.ReportRow;
//import com.vasyerp.Repository.*;
//import com.vasyerp.Service.ReportService;
//import com.vasyerp.Component.NotificationListener.ContactResult;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//        import java.util.stream.Collectors;
//
//import static com.vasyerp.Repository.NotificationRequestSpecifications.*;
//
//@Service
//public class ReportServiceImpl implements ReportService {
//
//    private final NotificationRequestRepository notificationRequestRepository;
//    private final RequestStatusRepository requestStatusRepository;
//    private final ContactRepository contactRepository;
//    private final CampaignRepository campaignRepository;
//    private final SmsTemplateRepository smsTemplateRepository;
//    private final EmailTemplateRepository emailTemplateRepository;
//    private final PushTemplateRepository pushTemplateRepository;
//    private final RabbitTemplate rabbitTemplate;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public ReportServiceImpl(
//            NotificationRequestRepository notificationRequestRepository,
//            RequestStatusRepository requestStatusRepository,
//            ContactRepository contactRepository, CampaignRepository campaignRepository, SmsTemplateRepository smsTemplateRepository, EmailTemplateRepository emailTemplateRepository, PushTemplateRepository pushTemplateRepository, RabbitTemplate rabbitTemplate
//    ) {
//        this.notificationRequestRepository = notificationRequestRepository;
//        this.requestStatusRepository = requestStatusRepository;
//        this.contactRepository = contactRepository;
//        this.campaignRepository = campaignRepository;
//        this.smsTemplateRepository = smsTemplateRepository;
//        this.emailTemplateRepository = emailTemplateRepository;
//        this.pushTemplateRepository = pushTemplateRepository;
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    @Value("${app.rabbitmq.notification-exchange}")
//    private String notificationExchange;
//
//    @Value("${app.rabbitmq.notification-routing-key}")
//    private String notificationRoutingKey;
//
//    public ReportPage getReport(ReportFilterRequest filter) {
//        System.out.println(filter);
////        List<NotificationRequest> requests = notificationRequestRepository.findFiltered(
////                blankToNull(filter.getRequestId()),
////                blankToNull(filter.getCampaignId()),
////                blankToNull(filter.getRequestStatus()),
////                filter.getDateFrom(),
////                filter.getDateTo(),
////                blankToNull(filter.getNotificationType())
////        );
//
//
////        Specification<NotificationRequest> spec = Specification
////                .allOf(requestIdEquals(filter.getRequestId()))
////                .and(campaignIdEquals(filter.getCampaignId()))
////                .and(statusEquals(filter.getRequestStatus()))
////                .and(createdAfter(filter.getDateFrom()))
////                .and(createdBefore(filter.getDateTo()))
////                .and(notificationTypeMatches(filter.getNotificationType()));
//
////        Specification<NotificationRequest> spec = Specification.allOf(
////                requestIdEquals(filter.getRequestId()),
////                campaignIdEquals(filter.getCampaignId()),
////                statusEquals(filter.getRequestStatus()),
////                createdAfter(filter.getDateFrom()),
////                createdBefore(filter.getDateTo()),
////                notificationTypeMatches(filter.getNotificationType())
////        );
////        List<NotificationRequest> requests = notificationRequestRepository.findAll(
////                spec,
////                Sort.by(Sort.Direction.DESC, "createdAt")
////        );
////
////        System.out.println(requests);
////
////        List<ReportRow> allRows = new ArrayList<>();
////
////        for (NotificationRequest req : requests) {
////            System.out.println("in side loop");
////            RequestStatus statusEntity = requestStatusRepository.findById(req.getRequestId()).orElse(null);
////            System.out.println(statusEntity);
////            if (statusEntity == null) continue;
////
////            Map<String, String> templates = readTemplates(req);
////            // NEW shape: contactId -> channelType -> ChannelStatus
////            Map<String, Map<String, ChannelStatus>> statusByContact = readNestedStatusMap(statusEntity.getStatusJson());
////
////            System.out.println(statusByContact);
////            if (statusByContact.isEmpty()) continue;
////
////            Set<String> contactIds = statusByContact.keySet();
////            Map<String, Contact> contactsById = contactRepository.findAllById(contactIds).stream()
////                    .collect(Collectors.toMap(Contact::getContactId, c -> c));
////
////            String campaignName = null;
////            if (req.getCampaignId() != null) {
////                campaignName = campaignRepository.findById(req.getCampaignId())
////                        .map(c -> c.getName())
////                        .orElse(null);
////            }
////
////            System.out.println("in side 1");
////            for (Map.Entry<String, Map<String, ChannelStatus>> contactEntry : statusByContact.entrySet()) {
////                System.out.println("in side loop2");
////                String contactId = contactEntry.getKey();
////                Map<String, ChannelStatus> channelMap = contactEntry.getValue();
////                Contact contact = contactsById.get(contactId);
////
////                ReportRow row = new ReportRow();
////                row.setRequestId(req.getRequestId());
////                row.setCampaignId(req.getCampaignId());
////                row.setCampaignName(campaignName);
////                row.setScheduleTime(req.getScheduleTime());
////                row.setCreatedAt(req.getCreatedAt());
////                row.setRequestStatus(req.getStatus());
////                row.setContactId(contactId);
////                row.setContactName(contact != null ? contact.getName() : "(deleted contact)");
////                row.setContactEmail(contact != null ? contact.getEmail() : null);
////                row.setContactPhone(contact != null ? contact.getPhoneNumber() : null);
////
////                List<ReportRow.ChannelRow> channelRows = new ArrayList<>();
////                for (Map.Entry<String, ChannelStatus> chEntry : channelMap.entrySet()) {
////                    String channelType = chEntry.getKey();
////                    ChannelStatus cs = chEntry.getValue();
////
////                    List<String> history = cs.getStatus() == null ? List.of() : cs.getStatus();
////                    String latest = history.isEmpty() ? "UNKNOWN" : history.get(history.size() - 1);
////
////                    ReportRow.ChannelRow channelRow = new ReportRow.ChannelRow();
////                    channelRow.setChannelType(channelType);
////                    channelRow.setContent(templates.get(channelType));
////                    channelRow.setStatusHistory(history);
////                    channelRow.setLatestStatus(latest);
////                    channelRow.setRetryCount(cs.getRetryCount());
////                    channelRow.setRetryEligible("FAILED".equalsIgnoreCase(latest) && cs.getRetryCount() < 3);
////
////                    channelRows.add(channelRow);
////                }
////                row.setChannels(channelRows);
////
////                allRows.add(row);
////
////                System.out.println(allRows);
////            }
////        }
////
////        // Apply the two filters that can only be evaluated per-contact, in memory
////        List<ReportRow> filtered = allRows.stream()
////                .filter(r -> matchesNotificationStatus(r, filter.getNotificationStatus()))
////                .filter(r -> matchesContactSearch(r, filter.getContactSearch()))
////                .collect(Collectors.toList());
////
////        int page = Math.max(filter.getPage(), 0);
////        int size = filter.getSize() <= 0 ? 10 : filter.getSize();
////        int fromIndex = Math.min(page * size, filtered.size());
////        int toIndex = Math.min(fromIndex + size, filtered.size());
////        List<ReportRow> pageContent = filtered.subList(fromIndex, toIndex);
////
////        int totalPages = (int) Math.ceil((double) filtered.size() / size);
////
////        ReportPage reportPage=new ReportPage(pageContent, filtered.size(), totalPages, page, size);
////        System.out.println(reportPage);
////        return reportPage;
//        return null;
//    }
//
//
//    private Map<String, Map<String, ChannelStatus>> readNestedStatusMap(String json) {
//        try {
//            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, ChannelStatus>>>() {});
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Collections.emptyMap();
//        }
//    }
//
//    private boolean matchesNotificationStatus(ReportRow row, String notificationStatus) {
//        if (notificationStatus == null || notificationStatus.isBlank()) return true;
//        return row.getChannels().stream()
//                .anyMatch(c -> notificationStatus.equalsIgnoreCase(c.getLatestStatus()));
//    }
//
//    private boolean matchesContactSearch(ReportRow row, String search) {
//        if (search == null || search.isBlank()) return true;
//        String term = search.toLowerCase();
//        return (row.getContactEmail() != null && row.getContactEmail().toLowerCase().contains(term))
//                || (row.getContactPhone() != null && row.getContactPhone().toLowerCase().contains(term));
//    }
//
////    private Map<String, String> readTemplates(String json) {
////        try {
////            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
////        } catch (Exception e) {
////            return Collections.emptyMap();
////        }
////    }
//
//    private Map<String, ContactResult> readStatusMap(String json) {
//        try {
//            return objectMapper.readValue(json, new TypeReference<Map<String, ContactResult>>() {});
//        } catch (Exception e) {
//            return Collections.emptyMap();
//        }
//    }
//
//    private String blankToNull(String value) {
//        return (value == null || value.isBlank()) ? null : value;
//    }
//
//    public void retry(String requestId, String contactId) {
////        RequestStatus statusEntity = requestStatusRepository.findById(requestId)
////                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
////
////        Map<String, ContactResult> statusByContact = readStatusMap(statusEntity.getStatusJson());
////        ContactResult result = statusByContact.get(contactId);
////
////        if (result == null) {
////            throw new IllegalArgumentException("Contact not found in this request.");
////        }
////
////        String latestStatus = result.getStatus().isEmpty() ? "UNKNOWN" : result.getStatus().get(result.getStatus().size() - 1);
////        if (!"FAILED".equalsIgnoreCase(latestStatus)) {
////            throw new IllegalArgumentException("Only failed notifications can be retried.");
////        }
////        if (result.getRetryCount() >= 3) {
////            throw new IllegalArgumentException("Retry limit reached for this contact.");
////        }
////
////        Contact contact = contactRepository.findById(contactId)
////                .orElseThrow(() -> new IllegalArgumentException("Contact no longer exists."));
////
////        Map<String, String> templates = readTemplates(statusEntity.getTemplatesJson());
////
////        // Mark as PENDING immediately and increment retryCount optimistically —
////        // avoids double-retry if the user clicks twice before the async result lands
////        List<String> updatedHistory = new ArrayList<>(result.getStatus());
////        updatedHistory.add("PENDING");
////        result.setStatus(updatedHistory);
////        result.setRetryCount(result.getRetryCount() + 1);
////        statusByContact.put(contactId, result);
////
////        try {
////            statusEntity.setStatusJson(objectMapper.writeValueAsString(statusByContact));
////            requestStatusRepository.save(statusEntity);
////        } catch (Exception e) {
////            throw new RuntimeException("Could not update retry status.", e);
////        }
////
////        // Re-publish just this one contact for reprocessing — sent immediately,
////        // no schedule delay, since a retry is by definition already overdue
////        com.vasyerp.Model.ContactDto contactDto = new com.vasyerp.Model.ContactDto(
////                contact.getContactId(), contact.getName(), contact.getEmail(), contact.getPhoneNumber()
////        );
////        com.vasyerp.Model.NotificationRequestModel retryModel = new com.vasyerp.Model.NotificationRequestModel(
////                requestId, templates, List.of(contactDto)
////        );
////
////        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, retryModel);
//
//
//
//    }
//
//
//}






package com.vasyerp.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.*;
import com.vasyerp.Repository.CampaignRepository;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Repository.RequestStatusRepository;
import com.vasyerp.Service.ReportService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.vasyerp.Repository.NotificationRequestSpecifications.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RequestStatusRepository requestStatusRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportServiceImpl(
            NotificationRequestRepository notificationRequestRepository, RabbitTemplate rabbitTemplate,
            RequestStatusRepository requestStatusRepository,
            CampaignRepository campaignRepository
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.requestStatusRepository = requestStatusRepository;
        this.campaignRepository = campaignRepository;
    }
    @Value("${app.rabbitmq.status-exchange}")
    private String statusExchange;

    @Value("${app.rabbitmq.status-routing-key}")
    private String statusRoutingKey;

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(readOnly = true)
    public ReportPage getReport(ReportFilterRequest filter) {
        Specification<NotificationRequest> spec = Specification
                .allOf(requestIdEquals(filter.getRequestId()))
                .and(campaignIdEquals(filter.getCampaignId()))
                .and(statusEquals(filter.getRequestStatus()))
                .and(createdAfter(filter.getDateFrom()))
                .and(createdBefore(filter.getDateTo()))
                .and(notificationTypeMatches(filter.getNotificationType()));

        List<NotificationRequest> requests = notificationRequestRepository.findAll(
                spec, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<ReportRow> allRows = new ArrayList<>();

        for (NotificationRequest req : requests) {
            List<RequestStatus> statuses = req.getRequestStatuses();
            if (statuses.isEmpty()) continue; // still scheduled/queued, nothing per-contact yet

            String campaignName = req.getCampaignId() != null
                    ? campaignRepository.findById(req.getCampaignId()).map(c -> c.getName()).orElse(null)
                    : null;

            for (RequestStatus rs : statuses) {
                Map<String, ChannelStatusEntry> channelMap = readChannelMap(rs.getChannelWiseStatusJson());
                if (channelMap.isEmpty()) continue;

                ReportRow row = new ReportRow();
                row.setRequestId(req.getRequestId());
                row.setCampaignId(req.getCampaignId());
                row.setCampaignName(campaignName);
                row.setScheduleTime(req.getScheduleTime());
                row.setCreatedAt(req.getCreatedAt());
                row.setRequestStatus(req.getStatus());
                row.setContactId(rs.getContactId());

                // contact name/email/phone: pull from userData in whichever
                // channel carries it, since we no longer join Contact directly
                String email = channelMap.containsKey("email") ? channelMap.get("email").getUserData() : null;
                String phone = channelMap.containsKey("sms") ? channelMap.get("sms").getUserData() : null;
                row.setContactEmail(email);
                row.setContactPhone(phone);
                row.setContactName(email != null ? email : (phone != null ? phone : rs.getContactId()));

                List<ReportRow.ChannelRow> channelRows = new ArrayList<>();
                for (Map.Entry<String, ChannelStatusEntry> entry : channelMap.entrySet()) {
                    String channelType = entry.getKey();
                    ChannelStatusEntry cs = entry.getValue();

                    List<String> history = cs.getStatus() == null ? List.of() : cs.getStatus();
                    String latest = history.isEmpty() ? "UNKNOWN" : history.get(history.size() - 1);

                    String content = switch (channelType) {
                        case "sms" -> req.getSmsTemplate() != null ? req.getSmsTemplate().getContent() : null;
                        case "email" -> req.getEmailTemplate() != null ? req.getEmailTemplate().getContent() : null;
                        case "push" -> req.getPushTemplate() != null ? req.getPushTemplate().getContent() : null;
                        default -> null;
                    };

                    ReportRow.ChannelRow channelRow = new ReportRow.ChannelRow();
                    channelRow.setChannelType(channelType);
                    channelRow.setContent(content);
                    channelRow.setStatusHistory(history);
                    channelRow.setLatestStatus(latest);
                    channelRow.setRetryCount(cs.getRetryCount());
                    channelRow.setUserData(cs.getUserData());
                    int maxRetries = req.getRetryAttempt() != null ? req.getRetryAttempt() : 3;
                    channelRow.setRetryEligible("FAILED".equalsIgnoreCase(latest) && cs.getRetryCount() < maxRetries);

                    channelRows.add(channelRow);
                }
                row.setChannels(channelRows);
                allRows.add(row);
            }
        }

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

    private boolean matchesNotificationStatus(ReportRow row, String notificationStatus) {
        if (notificationStatus == null || notificationStatus.isBlank()) return true;
        return row.getChannels().stream().anyMatch(c -> notificationStatus.equalsIgnoreCase(c.getLatestStatus()));
    }

    private boolean matchesContactSearch(ReportRow row, String search) {
        if (search == null || search.isBlank()) return true;
        String term = search.toLowerCase();
        return (row.getContactEmail() != null && row.getContactEmail().toLowerCase().contains(term))
                || (row.getContactPhone() != null && row.getContactPhone().toLowerCase().contains(term));
    }

    private Map<String, ChannelStatusEntry> readChannelMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, ChannelStatusEntry>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public void retry(String requestId, String contactId, String channelType) {
        RequestStatus rs = requestStatusRepository.findByRequestRequestIdAndContactId(requestId, contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found in this request."));

        NotificationRequest req = notificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        Map<String, ChannelStatusEntry> channelMap = readChannelMap(rs.getChannelWiseStatusJson());
        ChannelStatusEntry entry = channelMap.get(channelType);
        if (entry == null) throw new IllegalArgumentException("Channel not found for this contact.");

        String latest = entry.getStatus().isEmpty() ? "UNKNOWN" : entry.getStatus().get(entry.getStatus().size() - 1);
        if (!"FAILED".equalsIgnoreCase(latest)) {
            throw new IllegalArgumentException("Only failed notifications can be retried.");
        }
        int maxRetries = req.getRetryAttempt() != null ? req.getRetryAttempt() : 3;
        if (entry.getRetryCount() >= maxRetries) {
            throw new IllegalArgumentException("Retry limit reached for this channel.");
        }

        List<String> updatedHistory = new ArrayList<>(entry.getStatus());
        updatedHistory.add(entry.getRetryCount() + 1,"PENDING");
        entry.setStatus(updatedHistory);
        entry.setRetryCount(entry.getRetryCount() + 1);
        channelMap.put(channelType, entry);

        try {
            rs.setChannelWiseStatusJson(objectMapper.writeValueAsString(channelMap));
            requestStatusRepository.save(rs);
        } catch (Exception e) {
            throw new RuntimeException("Could not update retry status.", e);
        }

        // Re-publish just this contact+channel through the status queue's random
        // delivered/failed simulation path, reusing the same flow as first-send
        rabbitTemplate.convertAndSend(
                statusExchange, statusRoutingKey,
                new ContactStatusMessage(requestId, contactId, channelType, "SENT")
        );
    }

//    public static class ReportPage {
//        private final List<ReportRow> content;
//        private final long totalElements;
//        private final int totalPages;
//        private final int number;
//        private final int size;
//
//        public ReportPage(List<ReportRow> content, long totalElements, int totalPages, int number, int size) {
//            this.content = content;
//            this.totalElements = totalElements;
//            this.totalPages = totalPages;
//            this.number = number;
//            this.size = size;
//        }
//
//        public List<ReportRow> getContent() { return content; }
//        public long getTotalElements() { return totalElements; }
//        public int getTotalPages() { return totalPages; }
//        public int getNumber() { return number; }
//        public int getSize() { return size; }
//    }



    public DashboardSummary getSummary() {
        List<NotificationRequest> all = notificationRequestRepository.findAll();

        DashboardSummary summary = new DashboardSummary();
        summary.setTotalRequests(all.size());
        summary.setTotalRecipients(all.stream().mapToLong(NotificationRequest::getRecipientCount).sum());

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(NotificationRequest::getStatus, Collectors.counting()));
        summary.setRequestsByStatus(byStatus);

        Map<String, Map<String, Long>> statusByChannel = new HashMap<>();
        Map<String, Long> retriesByChannel = new HashMap<>();

        for (NotificationRequest req : all) {
            if (!Boolean.TRUE.equals(req.getSummaryFetched())) continue; // only use finalized summaries

            Map<String, Map<String, Long>> perRequestStatus = readStatusCounts(req.getTotalChannelWiseStatusCount());
            for (Map.Entry<String, Map<String, Long>> chEntry : perRequestStatus.entrySet()) {
                String channel = chEntry.getKey();
                Map<String, Long> target = statusByChannel.computeIfAbsent(channel, k -> new HashMap<>());
                for (Map.Entry<String, Long> statusEntry : chEntry.getValue().entrySet()) {
                    target.merge(statusEntry.getKey(), statusEntry.getValue(), Long::sum);
                }
            }

            Map<String, Long> perRequestRetries = readRetryCounts(req.getChannelWiseRetryCount());
            for (Map.Entry<String, Long> entry : perRequestRetries.entrySet()) {
                retriesByChannel.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }

        summary.setStatusByChannel(statusByChannel);
        summary.setRetriesByChannel(retriesByChannel);

        summary.setRequestsOverTime(buildTrend(all));

        return summary;
    }

    private List<DashboardSummary.DailyCount> buildTrend(List<NotificationRequest> all) {
        Instant since = Instant.now().minus(14, ChronoUnit.DAYS);
        Map<String, Long> counts = all.stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since))
                .collect(Collectors.groupingBy(
                        r -> DAY_FORMAT.withZone(ZoneId.systemDefault()).format(r.getCreatedAt()),
                        Collectors.counting()
                ));

        List<DashboardSummary.DailyCount> result = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            String day = DAY_FORMAT.withZone(ZoneId.systemDefault()).format(Instant.now().minus(i, ChronoUnit.DAYS));
            result.add(new DashboardSummary.DailyCount(day, counts.getOrDefault(day, 0L)));
        }
        return result;
    }

    private Map<String, Map<String, Long>> readStatusCounts(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, Long>>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Map<String, Long> readRetryCounts(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
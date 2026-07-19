package com.vasyerp.Repository;

import com.vasyerp.Entity.NotificationRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class NotificationRequestSpecifications {

    private NotificationRequestSpecifications() {}

    public static Specification<NotificationRequest> requestIdEquals(String requestId) {
        return (root, query, cb) ->
                (requestId == null || requestId.isBlank()) ? null : cb.equal(root.get("requestId"), requestId);
    }

    public static Specification<NotificationRequest> campaignIdEquals(String campaignId) {
        return (root, query, cb) ->
                (campaignId == null || campaignId.isBlank()) ? null : cb.equal(root.get("campaignId"), campaignId);
    }

    public static Specification<NotificationRequest> statusEquals(String status) {
        return (root, query, cb) ->
                (status == null || status.isBlank()) ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<NotificationRequest> notificationTypeMatches(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return null;
            return switch (type) {
                case "sms" -> cb.isNotNull(root.get("smsTemplate"));
                case "email" -> cb.isNotNull(root.get("emailTemplate"));
                case "push" -> cb.isNotNull(root.get("pushTemplate"));
                default -> null;
            };
        };
    }

    public static Specification<NotificationRequest> createdAfter(Instant dateFrom) {
        return (root, query, cb) ->
                (dateFrom == null) ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom);
    }

    public static Specification<NotificationRequest> createdBefore(Instant dateTo) {
        return (root, query, cb) ->
                (dateTo == null) ? null : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo);
    }

//    public static Specification<NotificationRequest> notificationTypeMatches(String type) {
//        return (root, query, cb) -> {
//            if (type == null || type.isBlank()) return null;
//            return switch (type) {
//                case "sms" -> cb.isNotNull(root.get("smsTemplateId"));
//                case "email" -> cb.isNotNull(root.get("emailTemplateId"));
//                case "push" -> cb.isNotNull(root.get("pushTemplateId"));
//                default -> null;
//            };
//        };
//    }
}
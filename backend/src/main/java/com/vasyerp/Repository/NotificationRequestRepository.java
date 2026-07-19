package com.vasyerp.Repository;

import com.vasyerp.Entity.NotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, String>, JpaSpecificationExecutor<NotificationRequest> {
    List<NotificationRequest> findByStatusAndScheduleTimeLessThanEqual(String status, Instant scheduleTime);

    List<NotificationRequest> findByStatusAndSummaryFetchedFalseAndCreatedAtLessThanEqual(String status, Instant cutoff);

//    @Query("""
//        SELECT n FROM NotificationRequest n
//        WHERE (:requestId IS NULL OR n.requestId = :requestId)
//          AND (:campaignId IS NULL OR n.campaignId = :campaignId)
//          AND (:requestStatus IS NULL OR n.status = :requestStatus)
//          AND (:dateFrom IS NULL OR n.createdAt >= :dateFrom)
//          AND (:dateTo IS NULL OR n.createdAt <= :dateTo)
//          AND (:notificationType IS NULL
//               OR (:notificationType = 'sms' AND n.smsTemplateId IS NOT NULL)
//               OR (:notificationType = 'email' AND n.emailTemplateId IS NOT NULL)
//               OR (:notificationType = 'push' AND n.pushTemplateId IS NOT NULL))
//        ORDER BY n.createdAt DESC
//        """)
//    List<NotificationRequest> findFiltered(
//            @Param("requestId") String requestId,
//            @Param("campaignId") String campaignId,
//            @Param("requestStatus") String requestStatus,
//            @Param("dateFrom") Instant dateFrom,
//            @Param("dateTo") Instant dateTo,
//            @Param("notificationType") String notificationType
//    );


//    @Query("""
//        SELECT n FROM NotificationRequest n
//        WHERE (:requestId IS NULL OR n.requestId = :requestId)
//          AND (:campaignId IS NULL OR n.campaignId = :campaignId)
//          AND (:requestStatus IS NULL OR n.status = :requestStatus)
//          AND (CAST(:dateFrom AS timestamp) IS NULL OR n.createdAt >= CAST(:dateFrom AS timestamp))
//          AND (CAST(:dateTo AS timestamp) IS NULL OR n.createdAt <= CAST(:dateTo AS timestamp))
//          AND (:notificationType IS NULL
//               OR (:notificationType = 'sms' AND n.smsTemplateId IS NOT NULL)
//               OR (:notificationType = 'email' AND n.emailTemplateId IS NOT NULL)
//               OR (:notificationType = 'push' AND n.pushTemplateId IS NOT NULL))
//        ORDER BY n.createdAt DESC
//        """)
//    List<NotificationRequest> findFiltered(
//            @Param("requestId") String requestId,
//            @Param("campaignId") String campaignId,
//            @Param("requestStatus") String requestStatus,
//            @Param("dateFrom") Instant dateFrom,
//            @Param("dateTo") Instant dateTo,
//            @Param("notificationType") String notificationType
//    );
}
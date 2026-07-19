package com.vasyerp.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Entity.RequestStatus;
import com.vasyerp.Model.ChannelStatusEntry;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Repository.RequestStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs periodically, picks up requests that finished processing (COMPLETED)
 * at least 1 minute ago and haven't had their summary computed yet, then
 * aggregates per-contact channel statuses into two cached JSON columns on
 * NotificationRequest so the dashboard/report never has to re-scan every
 * RequestStatus row for a given request on every page load.
 */
@Component
public class NotificationSummaryScheduler {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationSummaryScheduler(
            NotificationRequestRepository notificationRequestRepository,
            RequestStatusRepository requestStatusRepository
    ) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.requestStatusRepository = requestStatusRepository;
    }

    @Scheduled(fixedDelay = 30000) // every 30 seconds
    @Transactional(readOnly = true)
    public void summarizeCompletedRequests() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.MINUTES);

        List<NotificationRequest> candidates = notificationRequestRepository
                .findByStatusAndSummaryFetchedFalseAndCreatedAtLessThanEqual("COMPLETED", cutoff);

        for (NotificationRequest req : candidates) {
            try {
                summarizeOne(req);
            } catch (Exception e) {
                // Don't let one bad request block the rest of the batch —
                // it'll simply be picked up again on the next run since
                // summaryFetched is only set true on success
                e.printStackTrace();
            }
        }
    }

    private void summarizeOne(NotificationRequest req) {
        List<RequestStatus> statuses = req.getRequestStatuses();

        // channel -> total retries used across all contacts
        Map<String, Integer> retryCountByChannel = new HashMap<>();
        // channel -> status -> count of contacts currently at that latest status
        Map<String, Map<String, Integer>> statusCountByChannel = new HashMap<>();

        for (RequestStatus rs : statuses) {
            Map<String, ChannelStatusEntry> channelMap = readChannelMap(rs.getChannelWiseStatusJson());

            for (Map.Entry<String, ChannelStatusEntry> entry : channelMap.entrySet()) {
                String channelType = entry.getKey();
                ChannelStatusEntry cs = entry.getValue();

                retryCountByChannel.merge(channelType, cs.getRetryCount(), Integer::sum);

                List<String> history = cs.getStatus();
                String latestStatus = (history == null || history.isEmpty())
                        ? "UNKNOWN"
                        : history.get(history.size() - 1);

                statusCountByChannel
                        .computeIfAbsent(channelType, k -> new HashMap<>())
                        .merge(latestStatus, 1, Integer::sum);
            }
        }

        try {
            req.setChannelWiseRetryCount(objectMapper.writeValueAsString(retryCountByChannel));
            req.setTotalChannelWiseStatusCount(objectMapper.writeValueAsString(statusCountByChannel));
            req.setSummaryFetched(true);
            notificationRequestRepository.save(req);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize summary for request " + req.getRequestId(), e);
        }
    }

    private Map<String, ChannelStatusEntry> readChannelMap(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, ChannelStatusEntry.class)
            );
        } catch (Exception e) {
            return Map.of();
        }
    }
}
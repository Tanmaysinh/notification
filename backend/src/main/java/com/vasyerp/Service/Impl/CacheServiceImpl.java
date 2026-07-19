package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.NotificationRequest;
import com.vasyerp.Repository.NotificationRequestRepository;
import com.vasyerp.Service.CacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {
    private final NotificationRequestRepository notificationRequestRepository;

    public CacheServiceImpl(NotificationRequestRepository notificationRequestRepository) {
        this.notificationRequestRepository = notificationRequestRepository;
    }

    @Cacheable(value = "retryLimits", key = "#requestId")
    public int getMaxRetries(String requestId) {
        return notificationRequestRepository.findById(requestId)
                .map(NotificationRequest::getRetryAttempt)
                .orElse(3);
    }
}

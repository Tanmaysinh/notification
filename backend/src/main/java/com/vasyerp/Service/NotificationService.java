package com.vasyerp.Service;

import com.vasyerp.Model.SendNotificationRequest;

public interface NotificationService {
    String send(SendNotificationRequest req);
}

package com.vasyerp.Model;

public class SendNotificationResponse {
    private final String requestId;

    public SendNotificationResponse(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() { return requestId; }
}
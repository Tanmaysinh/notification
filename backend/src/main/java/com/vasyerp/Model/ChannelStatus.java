package com.vasyerp.Model;

import java.util.List;

public class ChannelStatus {
    private List<String> status;
    private int retryCount;

    public ChannelStatus() {}

    public ChannelStatus(String initialStatus, int retryCount) {
        this.status = List.of(initialStatus);
        this.retryCount = retryCount;
    }

    public List<String> getStatus() { return status; }
    public void setStatus(List<String> status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
package com.vasyerp.Model;

import java.util.List;

public class ChannelStatusEntry {
    private List<String> status;
    private int retryCount;
    private String userData; // phone (sms), email (email), deviceToken (push)

    public ChannelStatusEntry() {}

    public ChannelStatusEntry(String initialStatus, int retryCount, String userData) {
        this.status = List.of(initialStatus);
        this.retryCount = retryCount;
        this.userData = userData;
    }

    public List<String> getStatus() { return status; }
    public void setStatus(List<String> status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getUserData() { return userData; }
    public void setUserData(String userData) { this.userData = userData; }
}
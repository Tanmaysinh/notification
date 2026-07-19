//package com.vasyerp.Model;
//
//public class ContactStatusMessage {
//    private String requestId;
//    private String contactId;
//    private String status; // e.g. SENT, FAILED
//
//    public ContactStatusMessage() {}
//
//    public ContactStatusMessage(String requestId, String contactId, String status) {
//        this.requestId = requestId;
//        this.contactId = contactId;
//        this.status = status;
//    }
//
//    public String getRequestId() { return requestId; }
//    public void setRequestId(String requestId) { this.requestId = requestId; }
//    public String getContactId() { return contactId; }
//    public void setContactId(String contactId) { this.contactId = contactId; }
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//}


package com.vasyerp.Model;

public class ContactStatusMessage {
    private String requestId;
    private String contactId;
    private String channelType;
    private String status;

    public ContactStatusMessage() {}

    public ContactStatusMessage(String requestId, String contactId, String channelType, String status) {
        this.requestId = requestId;
        this.contactId = contactId;
        this.channelType = channelType;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ContactStatusMessage{" +
                "requestId='" + requestId + '\'' +
                ", contactId='" + contactId + '\'' +
                ", channelType='" + channelType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
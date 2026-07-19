//package com.vasyerp.Entity;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(name = "RequestStatusMaster")
//public class RequestStatus {
//
//    @Id
//    private String requestId; // same id as NotificationRequest, one-to-one by convention
//
////    @Column(columnDefinition = "TEXT", nullable = false)
////    private String templatesJson; // {"sms": "...", "email": "...", "push": "..."}
//
//    private String contactId1;
//
//    @Column(columnDefinition = "TEXT", nullable = false)
//    private String channelWiseStatusJson; //  {"status": ["SENT"], "retryCount": 0,userdata:email/phone,number,devicetokrn}, ...}
//
//
//
//    protected RequestStatus() {}
//
//    public RequestStatus(String requestId,  String statusJson) {
//        this.requestId = requestId;
////        this.templatesJson = templatesJson;
//        this.channelWiseStatusJson = statusJson;
//    }
//
//    public String getRequestId() { return requestId; }
////    public String getTemplatesJson() { return templatesJson; }
////    public void setTemplatesJson(String templatesJson) { this.templatesJson = templatesJson; }
//    public String getStatusJson() { return channelWiseStatusJson; }
//    public void setStatusJson(String statusJson) { this.channelWiseStatusJson = statusJson;  }
////    public Instant getUpdatedAt() { return updatedAt; }
//
//    @Override
//    public String toString() {
//        return "RequestStatus{" +
//                "requestId='" + requestId + '\'' +
////                ", templatesJson='" + templatesJson + '\'' +
//                ", statusJson='" + channelWiseStatusJson + '\'' +
////                ", updatedAt=" + updatedAt +
//                '}';
//    }
//}


package com.vasyerp.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "RequestStatusMaster", uniqueConstraints = @UniqueConstraint(columnNames = {"requestId", "contactId"}))
public class RequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private NotificationRequest request;

    @Column(nullable = false)
    private String contactId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String channelWiseStatusJson; // {"sms": {"status": ["PENDING"], "retryCount": 0, "userData": "..."}, "email": {...}, "push": {...}}

    @Column(nullable = false)
    private Instant updatedAt;

    protected RequestStatus() {}

    public RequestStatus(NotificationRequest request, String contactId, String channelWiseStatusJson) {
        this.request = request;
        this.contactId = contactId;
        this.channelWiseStatusJson = channelWiseStatusJson;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public NotificationRequest getRequest() { return request; }
    public String getContactId() { return contactId; }
    public String getChannelWiseStatusJson() { return channelWiseStatusJson; }
    public void setChannelWiseStatusJson(String json) { this.channelWiseStatusJson = json; this.updatedAt = Instant.now(); }
    public Instant getUpdatedAt() { return updatedAt; }
}
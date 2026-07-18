package com.vasyerp.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "ContactMaster")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String contactId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String deviceToken;

    protected Contact() {}

    public Contact(String name, String email, String phoneNumber,String deviceToken) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceToken=deviceToken;
    }

    public String getContactId() { return contactId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
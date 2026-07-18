package com.vasyerp.Model;

public class ContactDto {
    private String contactId;
    private String name;
    private String email;
    private String phoneNumber;

    public ContactDto() {}

    public ContactDto(String contactId, String name, String email, String phoneNumber) {
        this.contactId = contactId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
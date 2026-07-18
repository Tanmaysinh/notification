package com.vasyerp.Entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserMaster")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;


    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @PrePersist
    protected void onCreate() {
        createdTimestamp = LocalDateTime.now();
    }

    public User() {
    }

    public User(String name, String email, String password ) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getPassword() {
        return password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
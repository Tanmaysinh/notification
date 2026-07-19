package com.vasyerp.Model;


public class AuthResponse {
    private final String token;
    private final UserSummary user;

    public AuthResponse(String token, UserSummary user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public UserSummary getUser() { return user; }

    public static class UserSummary {
        private final Long id;
        private final String email;
        private final String name;

        public UserSummary(Long id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }
}
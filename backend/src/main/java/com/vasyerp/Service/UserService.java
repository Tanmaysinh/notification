package com.vasyerp.Service;

import com.vasyerp.Entity.User;

public interface UserService {
    User createUser(String name, String email, String rawPassword);

    User verifyCredentials(String email, String rawPassword);
}

package com.vasyerp.Service.Impl;

import com.vasyerp.Entity.User;
import com.vasyerp.Repository.UserRepository;
import com.vasyerp.Service.UserService;
import org.springframework.stereotype.Service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String name, String email, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        System.out.println(email);
        System.out.println(rawPassword);
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User(name, email, hash);
        return userRepository.save(user);
    }

    public User verifyCredentials(String email, String rawPassword) {
        System.out.println(email);
        System.out.println(rawPassword);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }
}
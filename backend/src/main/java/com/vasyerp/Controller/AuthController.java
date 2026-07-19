package com.vasyerp.Controller;

import com.vasyerp.Model.AuthResponse;
import com.vasyerp.Model.LoginRequest;
import com.vasyerp.Model.SignupRequest;
import com.vasyerp.Component.JwtService;
import com.vasyerp.Entity.User;
import com.vasyerp.Service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user.getUserId(), user.getEmail());
        return new AuthResponse(token, new AuthResponse.UserSummary(user.getUserId(), user.getEmail(), user.getName()));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.verifyCredentials(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user.getUserId(), user.getEmail());
        return new AuthResponse(token, new AuthResponse.UserSummary(user.getUserId(), user.getEmail(), user.getName()));
    }
}
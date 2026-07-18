//package com.vasyerp.Controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vasyerp.Model.AuthResponse;
//import com.vasyerp.Model.LoginRequest;
//import com.vasyerp.Model.SignupRequest;
//import com.vasyerp.Component.JwtService;
//import com.vasyerp.crypto.AesGcmUtil;
//import com.vasyerp.Model.EncryptedEnvelope;
//import com.vasyerp.crypto.SessionKeyStore;
//import com.vasyerp.Entity.User;
//import com.vasyerp.Service.UserService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final SessionKeyStore sessionKeyStore;
//    private final UserService userService;
//    private final JwtService jwtService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public AuthController(SessionKeyStore sessionKeyStore, UserService userService, JwtService jwtService) {
//        this.sessionKeyStore = sessionKeyStore;
//        this.userService = userService;
//        this.jwtService = jwtService;
//    }
//
//    @PostMapping("/signup")
//    public EncryptedEnvelope signup(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        SignupRequest request = objectMapper.readValue(plaintext, SignupRequest.class);
//
//        System.out.println("request--->"+request);
//        User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
//        String token = jwtService.generateToken(user.getUserId(), user.getEmail());
//
//        AuthResponse response = new AuthResponse(
//                token,
//                new AuthResponse.UserSummary(user.getUserId(), user.getEmail(), user.getUsername())
//        );
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(response);
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//
//    @PostMapping("/login")
//    public EncryptedEnvelope login(
//            @RequestBody EncryptedEnvelope envelope,
//            @RequestHeader("X-Session-Id") String sessionId
//    ) throws Exception {
//        byte[] aesKey = sessionKeyStore.get(sessionId);
//
//        byte[] plaintext = AesGcmUtil.decrypt(aesKey, envelope);
//        LoginRequest request = objectMapper.readValue(plaintext, LoginRequest.class);
//        System.out.println(request);
//        User user = userService.verifyCredentials(request.getEmail(), request.getPassword());
//        String token = jwtService.generateToken(user.getUserId(), user.getEmail());
//
//        AuthResponse response = new AuthResponse(
//                token,
//                new AuthResponse.UserSummary(user.getUserId(), user.getEmail(), user.getUsername())
//        );
//
//        byte[] responseBytes = objectMapper.writeValueAsBytes(response);
//        return AesGcmUtil.encrypt(aesKey, responseBytes);
//    }
//}




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
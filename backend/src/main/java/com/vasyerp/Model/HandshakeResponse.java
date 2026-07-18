package com.vasyerp.Model;


import java.util.Map;

public class HandshakeResponse {
    private Map<String, Object> serverPublicKey;
    private String sessionId;
    private long expiresIn;

    public HandshakeResponse(Map<String, Object> serverPublicKey, String sessionId, long expiresIn) {
        this.serverPublicKey = serverPublicKey;
        this.sessionId = sessionId;
        this.expiresIn = expiresIn;
    }

    public Map<String, Object> getServerPublicKey() { return serverPublicKey; }
    public String getSessionId() { return sessionId; }
    public long getExpiresIn() { return expiresIn; }
}
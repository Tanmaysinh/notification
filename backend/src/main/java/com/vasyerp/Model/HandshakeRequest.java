package com.vasyerp.Model;


import java.util.Map;

public class HandshakeRequest {
    private Map<String, Object> clientPublicKey;

    public Map<String, Object> getClientPublicKey() { return clientPublicKey; }
    public void setClientPublicKey(Map<String, Object> clientPublicKey) { this.clientPublicKey = clientPublicKey; }
}
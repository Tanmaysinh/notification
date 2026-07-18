package com.vasyerp.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

/**
 * One EC keypair for the whole server lifetime, generated at startup.
 * Every client still derives a UNIQUE AES key per session (ECDH combines
 * this static server key with each client's own fresh ephemeral key), so
 * this doesn't weaken per-session key uniqueness — it just avoids
 * regenerating a server keypair on every single handshake call.
 * On restart, a new keypair is generated automatically; any session from
 * before the restart simply re-handshakes once, transparently.
 */
@Component
public class ServerKeyPairHolder {

    private KeyPair keyPair;

    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        this.keyPair = kpg.generateKeyPair();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
package com.vasyerp.crypto;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Short-lived in-memory store mapping sessionId -> derived AES key.
 * Entries auto-expire, forcing periodic re-handshake (forward secrecy
 * in practice — old session keys don't linger).
 *
 * NOTE: in-memory only means this won't survive app restarts or work
 * across multiple backend instances behind a load balancer. If you
 * scale horizontally, swap this for a Redis-backed store instead —
 * same interface, different backing store.
 */
@Component
public class SessionKeyStore {

    private final Cache<String, byte[]> store = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(100_000)
            .build();

    public void store(String sessionId, byte[] aesKeyBytes) {
        store.put(sessionId, aesKeyBytes);
    }

    public byte[] get(String sessionId) {
        byte[] key = store.getIfPresent(sessionId);
        if (key == null) {
            throw new IllegalStateException("Session expired or not found. Please re-establish the secure channel.");
        }
        return key;
    }

    public void invalidate(String sessionId) {
        store.invalidate(sessionId);
    }
}
package com.vasyerp.crypto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyerp.Model.EncryptedEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Transparently decrypts the incoming EncryptedEnvelope body into plain
 * JSON before it reaches @RequestBody-bound controller methods, and
 * encrypts the plain JSON response back into an EncryptedEnvelope before
 * it leaves — so controllers work with normal DTOs and know nothing
 * about encryption. Only applies to paths configured to require it
 * (currently just /api/auth/**).
 */
@Component
@Order(1) // must run before Spring's request body gets consumed by @RequestBody binding
public class SecureChannelFilter extends OncePerRequestFilter {

    private final SessionKeyStore sessionKeyStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecureChannelFilter(SessionKeyStore sessionKeyStore) {
        this.sessionKeyStore = sessionKeyStore;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only /api/auth/** goes through the encrypted channel for now
        return !path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Session-Id header.");
            return;
        }

        byte[] aesKey;
        try {
            aesKey = sessionKeyStore.get(sessionId);
        } catch (IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // ---- Decrypt request body ----
        byte[] rawBody = request.getInputStream().readAllBytes();
        byte[] decryptedBody;
        try {
            EncryptedEnvelope envelope = objectMapper.readValue(rawBody, EncryptedEnvelope.class);
            decryptedBody = AesGcmUtil.decrypt(aesKey, envelope);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not decrypt request payload.");
            return;
        }

        HttpServletRequest wrappedRequest = new DecryptedBodyRequestWrapper(request, decryptedBody);

        // ---- Capture response so it can be encrypted before writing out ----
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        byte[] plainResponseBody = wrappedResponse.getContentAsByteArray();

        if (plainResponseBody.length > 0) {
            EncryptedEnvelope encryptedEnvelope = AesGcmUtil.encrypt(aesKey, plainResponseBody);
            byte[] encryptedBytes = objectMapper.writeValueAsBytes(encryptedEnvelope);

            response.resetBuffer();
            response.setContentType("application/json");
            response.setContentLength(encryptedBytes.length);
            response.getOutputStream().write(encryptedBytes);
        }
        // Copy over the actual status code (e.g. 400/401 from an exception handler)
        // AFTER writing the encrypted body, since resetBuffer() above doesn't reset status
        wrappedResponse.copyBodyToResponse();
    }

    /** Wraps the request so Spring's @RequestBody sees the decrypted plaintext instead of the raw envelope. */
    private static class DecryptedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] decryptedBody;

        DecryptedBodyRequestWrapper(HttpServletRequest request, byte[] decryptedBody) {
            super(request);
            this.decryptedBody = decryptedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decryptedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // not needed for synchronous request handling
                }
            };
        }
    }
}
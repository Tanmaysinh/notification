package com.vasyerp.crypto;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.AlgorithmParameters;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts between JWK (JSON Web Key) format — what the browser's
 * crypto.subtle.exportKey("jwk", ...) produces — and Java's PublicKey
 * objects, specifically for P-256 (secp256r1) EC keys used in our
 * ECDHE handshake.
 */
public final class JwkUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwkUtils() {}

    /** Parses a client's JWK (sent as a Map from JSON body) into a PublicKey. */
    public static PublicKey toPublicKey(Map<String, Object> jwk) {
        try {
            byte[] xBytes = base64UrlDecode((String) jwk.get("x"));
            byte[] yBytes = base64UrlDecode((String) jwk.get("y"));

            ECPoint point = new ECPoint(
                    new java.math.BigInteger(1, xBytes),
                    new java.math.BigInteger(1, yBytes)
            );

            AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = params.getParameterSpec(ECParameterSpec.class);

            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecParameterSpec);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(pubSpec);
        } catch (Exception e) {
            throw new RuntimeException("Invalid client public key (JWK)", e);
        }
    }

    /** Exports a Java EC PublicKey as a JWK map, matching what the browser expects on import. */
    public static Map<String, Object> fromPublicKey(PublicKey publicKey) {
        try {
            java.security.interfaces.ECPublicKey ecPublicKey = (java.security.interfaces.ECPublicKey) publicKey;
            ECPoint point = ecPublicKey.getW();

            int fieldSizeBytes = 32; // P-256 => 32-byte coordinates
            byte[] xBytes = toFixedLength(point.getAffineX().toByteArray(), fieldSizeBytes);
            byte[] yBytes = toFixedLength(point.getAffineY().toByteArray(), fieldSizeBytes);

            Map<String, Object> jwk = new HashMap<>();
            jwk.put("kty", "EC");
            jwk.put("crv", "P-256");
            jwk.put("x", base64UrlEncode(xBytes));
            jwk.put("y", base64UrlEncode(yBytes));
            jwk.put("ext", true);
            jwk.put("key_ops", new String[]{});
            return jwk;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export server public key as JWK", e);
        }
    }

    private static byte[] toFixedLength(byte[] input, int length) {
        if (input.length == length) return input;
        byte[] output = new byte[length];
        if (input.length > length) {
            // strip leading sign byte BigInteger sometimes adds
            System.arraycopy(input, input.length - length, output, 0, length);
        } else {
            System.arraycopy(input, 0, output, length - input.length, input.length);
        }
        return output;
    }

    private static byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
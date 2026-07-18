package com.vasyerp.crypto;

import com.vasyerp.Model.EncryptedEnvelope;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encrypt/decrypt matching the frontend's crypto.subtle
 * AES-GCM calls. WebCrypto appends the 16-byte auth tag to the end of
 * the ciphertext automatically — Java's GCM cipher does the same, so
 * no separate tag handling is needed on either side as long as both
 * use a 12-byte IV and 128-bit (16 byte) tag length, which are the
 * WebCrypto AES-GCM defaults.
 */
public final class AesGcmUtil {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private AesGcmUtil() {}

    public static EncryptedEnvelope encrypt(byte[] aesKeyBytes, byte[] plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext); // tag appended automatically

            EncryptedEnvelope envelope = new EncryptedEnvelope();
            envelope.setIv(Base64.getEncoder().encodeToString(iv));
            envelope.setCiphertext(Base64.getEncoder().encodeToString(ciphertext));
            return envelope;
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    public static byte[] decrypt(byte[] aesKeyBytes, EncryptedEnvelope envelope) {
        try {
            byte[] iv = Base64.getDecoder().decode(envelope.getIv());
            byte[] ciphertext = Base64.getDecoder().decode(envelope.getCiphertext());

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            return cipher.doFinal(ciphertext); // throws if auth tag doesn't match
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }
}
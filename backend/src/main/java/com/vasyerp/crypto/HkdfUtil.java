package com.vasyerp.crypto;


import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

/**
 * HKDF (RFC 5869) key derivation — must produce byte-for-byte the same
 * output as the frontend's crypto.subtle.deriveKey({name:"HKDF", ...})
 * call for a given shared secret, salt, and info, or the two sides will
 * derive different AES keys and decryption will fail silently.
 */
public final class HkdfUtil {

    private HkdfUtil() {}

    public static byte[] derive(byte[] sharedSecret, byte[] salt, byte[] info, int outputLengthBytes) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, salt, info));

        byte[] output = new byte[outputLengthBytes];
        hkdf.generateBytes(output, 0, outputLengthBytes);
        return output;
    }
}
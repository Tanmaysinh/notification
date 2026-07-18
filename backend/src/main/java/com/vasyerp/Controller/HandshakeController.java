package com.vasyerp.Controller;

import com.vasyerp.Model.HandshakeRequest;
import com.vasyerp.Model.HandshakeResponse;
import com.vasyerp.crypto.HkdfUtil;
import com.vasyerp.crypto.JwkUtils;
import com.vasyerp.crypto.SessionKeyStore;
import org.springframework.web.bind.annotation.*;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.UUID;

@RestController
@RequestMapping("/crypto")
public class HandshakeController {

    private final SessionKeyStore sessionKeyStore;

    public HandshakeController(SessionKeyStore sessionKeyStore) {
        this.sessionKeyStore = sessionKeyStore;
    }

    @PostMapping("/handshake")
    public HandshakeResponse handshake(@RequestBody HandshakeRequest request) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair serverKeyPair = kpg.generateKeyPair();

        PublicKey clientPublicKey = JwkUtils.toPublicKey(request.getClientPublicKey());

        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(serverKeyPair.getPrivate());
        ka.doPhase(clientPublicKey, true);
        byte[] sharedSecret = ka.generateSecret();

        String sessionId = UUID.randomUUID().toString();

        byte[] aesKeyBytes = HkdfUtil.derive(
                sharedSecret,
                "vasy-erp-secure-channel".getBytes(),
                sessionId.getBytes(),
                32
        );

        sessionKeyStore.store(sessionId, aesKeyBytes);

        return new HandshakeResponse(
                JwkUtils.fromPublicKey(serverKeyPair.getPublic()),
                sessionId,
                900
        );
    }
}
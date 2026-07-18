import { crypto_ } from "./cryptoService";

const HANDSHAKE_URL = "/api/crypto/handshake";

interface HandshakeResponse {
  serverPublicKey: JsonWebKey;
  sessionId: string;
  expiresIn: number; // seconds
}

export interface SecureSession {
  sessionId: string;
  aesKey: CryptoKey;
  expiresAt: number;
}

export async function performHandshake(): Promise<SecureSession> {
  const keyPair = await crypto_.generateEphemeralKeyPair();
  const clientPublicKeyJwk = await crypto_.exportPublicKeyJwk(keyPair.publicKey);

  const res = await fetch(HANDSHAKE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ clientPublicKey: clientPublicKeyJwk }),
  });

  if (!res.ok) {
    throw new Error("Secure channel handshake failed");
  }

  const data: HandshakeResponse = await res.json();

  const serverPublicKey = await crypto_.importPeerPublicKey(data.serverPublicKey);
  const aesKey = await crypto_.deriveAesKey(
    keyPair.privateKey,
    serverPublicKey,
    data.sessionId
  );

  return {
    sessionId: data.sessionId,
    aesKey,
    expiresAt: Date.now() + data.expiresIn * 1000,
  };
}
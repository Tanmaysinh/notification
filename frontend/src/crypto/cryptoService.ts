// Core ECDHE + AES-GCM primitives, built entirely on native Web Crypto —
// no third-party crypto libs, since Web Crypto is browser-audited and
// hand-rolled/npm crypto here would be a downgrade, not an upgrade.

export interface SessionKeyMaterial {
  sessionId: string;
  aesKey: CryptoKey;
  expiresAt: number;
}

// Generates a fresh ECDH keypair. Never persisted, never exported except
// the public half, which is safe to expose by design.
async function generateEphemeralKeyPair(): Promise<CryptoKeyPair> {
  return crypto.subtle.generateKey(
    { name: "ECDH", namedCurve: "P-256" },
    true, // must be true to export the public key; see note at bottom re: private key exposure risk
    ["deriveKey", "deriveBits"]
  );
}

async function exportPublicKeyJwk(key: CryptoKey): Promise<JsonWebKey> {
  return crypto.subtle.exportKey("jwk", key);
}

async function importPeerPublicKey(jwk: JsonWebKey): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    "jwk",
    jwk,
    { name: "ECDH", namedCurve: "P-256" },
    false,
    []
  );
}

// Derives an AES-256-GCM key from the ECDH shared secret via HKDF.
// Using HKDF (rather than the raw ECDH output directly) is standard practice —
// it whitens/expands the shared secret into a properly distributed key.
async function deriveAesKey(
  privateKey: CryptoKey,
  peerPublicKey: CryptoKey,
  sessionId: string
): Promise<CryptoKey> {
  const sharedBits = await crypto.subtle.deriveBits(
    { name: "ECDH", public: peerPublicKey },
    privateKey,
    256
  );

  const hkdfBaseKey = await crypto.subtle.importKey(
    "raw",
    sharedBits,
    "HKDF",
    false,
    ["deriveKey"]
  );

  // sessionId as HKDF "info" binds the derived key to this specific session,
  // so two sessions never accidentally derive the same key even with key reuse bugs
  return crypto.subtle.deriveKey(
    {
      name: "HKDF",
      hash: "SHA-256",
      salt: new TextEncoder().encode("vasy-erp-secure-channel"),
      info: new TextEncoder().encode(sessionId),
    },
    hkdfBaseKey,
    { name: "AES-GCM", length: 256 },
    false, // AES key itself never needs to be exported either
    ["encrypt", "decrypt"]
  );
}

export interface EncryptedEnvelope {
  iv: string; // base64
  ciphertext: string; // base64, auth tag is appended by WebCrypto automatically
}

export async function encryptJson(
  aesKey: CryptoKey,
  data: unknown
): Promise<EncryptedEnvelope> {
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encoded = new TextEncoder().encode(JSON.stringify(data));

  const ciphertextBuffer = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv: iv as BufferSource },
    aesKey,
    encoded as BufferSource
  );

  return {
    iv: bufToB64(iv),
    ciphertext: bufToB64(ciphertextBuffer),
  };
}

export async function decryptJson<T = unknown>(
  aesKey: CryptoKey,
  envelope: EncryptedEnvelope
): Promise<T> {
  const iv = b64ToBuf(envelope.iv);
  const ciphertext = b64ToBuf(envelope.ciphertext);

  const decryptedBuffer = await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: iv as BufferSource },
    aesKey,
    ciphertext as BufferSource
  );

  return JSON.parse(new TextDecoder().decode(decryptedBuffer));
}

function bufToB64(buf: ArrayBuffer | Uint8Array): string {
  const bytes = buf instanceof Uint8Array ? buf : new Uint8Array(buf);
  let binary = "";
  bytes.forEach((b) => (binary += String.fromCharCode(b)));
  return btoa(binary);
}

function b64ToBuf(b64: string): Uint8Array {
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

export const crypto_ = {
  generateEphemeralKeyPair,
  exportPublicKeyJwk,
  importPeerPublicKey,
  deriveAesKey,
};
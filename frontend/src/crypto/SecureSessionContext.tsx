"use client";

import { createContext, useContext, useEffect, useRef, useState, useCallback } from "react";
import { performHandshake } from "./handshake";
import type { SecureSession } from "./handshake";
import { encryptJson, decryptJson } from "./cryptoService";
import type { EncryptedEnvelope } from "./cryptoService";

interface SecureSessionContextValue {
  ready: boolean;
  error: string | null;
  secureFetch: <TResponse = unknown>(url: string, body: unknown, init?: RequestInit) => Promise<TResponse>;
  apiFetch: <TResponse = unknown>(url: string,init?: RequestInit) => Promise<TResponse>;
  getSession: () => Promise<SecureSession>;
  clearSession: () => void;
}

const SecureSessionContext = createContext<SecureSessionContextValue | null>(null);

export function SecureSessionProvider({ children }: { children: React.ReactNode }) {
  const sessionRef = useRef<SecureSession | null>(null);
  const handshakePromiseRef = useRef<Promise<SecureSession> | null>(null);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Handshake happens ONCE per app load (or after an explicit clearSession on
  // logout) and the resulting key is held in memory for the whole session —
  // no periodic re-handshake timer anymore.
  const ensureSession = useCallback(async (): Promise<SecureSession> => {
    if (sessionRef.current) return sessionRef.current;

    if (!handshakePromiseRef.current) {
      handshakePromiseRef.current = performHandshake().finally(() => {
        handshakePromiseRef.current = null;
      });
    }

    const session = await handshakePromiseRef.current;
    sessionRef.current = session;
    return session;
  }, []);

  useEffect(() => {
    ensureSession()
      .then(() => setReady(true))
      .catch((err) => setError(err.message ?? "Could not establish secure channel"));
  }, [ensureSession]);

  
const secureFetch = useCallback(
  async <TResponse = unknown,>(url: string, body: unknown, init: RequestInit = {}): Promise<TResponse> => {
    const session = await ensureSession();
    const envelope = await encryptJson(session.aesKey, body);

    const token = localStorage.getItem("token");

    const res = await fetch(url, {
      method: "POST",
      ...init,
      headers: {
        "Content-Type": "application/json",
        "X-Session-Id": session.sessionId,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(init.headers ?? {}),
      },
      body: JSON.stringify(envelope),
    });

    const responseEnvelope: EncryptedEnvelope | { error: string } = await res.json();

    if (!res.ok) {
      const message = "error" in responseEnvelope ? (responseEnvelope as any).error : "Request failed";
      throw new Error(message);
    }

    return decryptJson<TResponse>(session.aesKey, responseEnvelope as EncryptedEnvelope);
  },
  [ensureSession]
);

async function apiFetchImpl<T = unknown>(
  url: string,
  init: RequestInit = {}
): Promise<T> {
  const session = await ensureSession();
  const token = localStorage.getItem("token");

  const res = await fetch(url, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers ?? {}),
    },
  });

  const body = await res.json();

  if (!res.ok) {
    throw new Error(body.error ?? "Request failed");
  }

  return decryptJson<T>(
    session.aesKey,
    body as EncryptedEnvelope
  );
}

const apiFetch = useCallback(apiFetchImpl, []);

  function clearSession() {
    sessionRef.current = null;
    setReady(false);
  }

  return (
    <SecureSessionContext.Provider value={{ ready, error, secureFetch, apiFetch,getSession: ensureSession, clearSession }}>
      {children}
    </SecureSessionContext.Provider>
  );
}

export function useSecureSession() {
  const ctx = useContext(SecureSessionContext);
  if (!ctx) throw new Error("useSecureSession must be used within SecureSessionProvider");
  return ctx;
}


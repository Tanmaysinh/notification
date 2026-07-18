// "use client";

// import { createContext, useContext, useEffect, useRef, useState, useCallback } from "react";
// import { performHandshake } from "./handshake";
// import type { SecureSession } from "./handshake";
// import { encryptJson, decryptJson } from "./cryptoService";
// import type { EncryptedEnvelope } from "./cryptoService";

// // interface SecureSessionContextValue {
// //   ready: boolean;
// //   error: string | null;
// //   secureFetch: <TResponse = unknown>(url: string, body: unknown, init?: RequestInit) => Promise<TResponse>;
// // }

// interface SecureSessionContextValue {
//   ready: boolean;
//   error: string | null;
//   secureFetch: <TResponse = unknown>(url: string, body: unknown, init?: RequestInit) => Promise<TResponse>;
//   getSession: () => Promise<SecureSession>;
// }



// const SecureSessionContext = createContext<SecureSessionContextValue | null>(null);

// const REHANDSHAKE_MARGIN_MS = 30_000; // re-handshake 30s before actual expiry

// export function SecureSessionProvider({ children }: { children: React.ReactNode }) {
//   const sessionRef = useRef<SecureSession | null>(null);
//   const handshakePromiseRef = useRef<Promise<SecureSession> | null>(null);
//   const [ready, setReady] = useState(false);
//   const [error, setError] = useState<string | null>(null);

//   const ensureSession = useCallback(async (): Promise<SecureSession> => {
//     const current = sessionRef.current;
//     if (current && Date.now() < current.expiresAt - REHANDSHAKE_MARGIN_MS) {
//       return current;
//     }

//     // De-dupe concurrent handshake calls (e.g. two requests fire at once on cold load)
//     if (!handshakePromiseRef.current) {
//       handshakePromiseRef.current = performHandshake().finally(() => {
//         handshakePromiseRef.current = null;
//       });
//     }

//     const session = await handshakePromiseRef.current;
//     sessionRef.current = session;
//     return session;
//   }, []);

//   useEffect(() => {
//     ensureSession()
//       .then(() => setReady(true))
//       .catch((err) => setError(err.message ?? "Could not establish secure channel"));
//   }, [ensureSession]);

//   const secureFetch = useCallback(
//     async <TResponse = unknown,>(url: string, body: unknown, init: RequestInit = {}): Promise<TResponse> => {
//       const session = await ensureSession();
//       const envelope = await encryptJson(session.aesKey, body);

//       const res = await fetch(url, {
//         method: "POST",
//         ...init,
//         headers: {
//           "Content-Type": "application/json",
//           "X-Session-Id": session.sessionId,
//           ...(init.headers ?? {}),
//         },
//         body: JSON.stringify(envelope),
//       });

//       const responseEnvelope: EncryptedEnvelope | { error: string } = await res.json();

//       if (!res.ok) {
//         const message =
//           "error" in responseEnvelope ? (responseEnvelope as any).error : "Request failed";
//         throw new Error(message);
//       }

//       return decryptJson<TResponse>(session.aesKey, responseEnvelope as EncryptedEnvelope);
//     },
//     [ensureSession]
//   );

// //   return (
// //     <SecureSessionContext.Provider value={{ ready, error, secureFetch }}>
// //       {children}
// //     </SecureSessionContext.Provider>
// //   );

//   return (
//   <SecureSessionContext.Provider value={{ ready, error, secureFetch, getSession: ensureSession }}>
//     {children}
//   </SecureSessionContext.Provider>
// );
// }

// export function useSecureSession() {
//   const ctx = useContext(SecureSessionContext);
//   if (!ctx) throw new Error("useSecureSession must be used within SecureSessionProvider");
//   return ctx;
// }



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

      const res = await fetch(url, {
        method: "POST",
        ...init,
        headers: {
          "Content-Type": "application/json",
          "X-Session-Id": session.sessionId,
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

  function clearSession() {
    sessionRef.current = null;
    setReady(false);
  }

  return (
    <SecureSessionContext.Provider value={{ ready, error, secureFetch, getSession: ensureSession, clearSession }}>
      {children}
    </SecureSessionContext.Provider>
  );
}

export function useSecureSession() {
  const ctx = useContext(SecureSessionContext);
  if (!ctx) throw new Error("useSecureSession must be used within SecureSessionProvider");
  return ctx;
}
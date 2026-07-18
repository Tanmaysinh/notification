



import { API_BASE_URL } from "@/lib/apiConfig";
import type { SecureSession } from "@/crypto/handshake";
import { encryptJson, decryptJson } from "@/crypto/cryptoService";
import type {ReportFilters,ReportRow} from "@/types/report"

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

function authHeaders(): HeadersInit {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function securePost<TResponse>(
  session: SecureSession,
  path: string,
  body: unknown
): Promise<TResponse> {
  const envelope = await encryptJson(session.aesKey, body);

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Session-Id": session.sessionId,
      ...authHeaders(),
    },
    body: JSON.stringify(envelope),
  });

  const responseEnvelope = await res.json();
  if (!res.ok) throw new Error(responseEnvelope.error ?? "Request failed");
  return decryptJson<TResponse>(session.aesKey, responseEnvelope);
}

async function securePut<TResponse>(
  session: SecureSession,
  path: string,
  body: unknown
): Promise<TResponse> {
  const envelope = await encryptJson(session.aesKey, body);

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "X-Session-Id": session.sessionId,
      ...authHeaders(),
    },
    body: JSON.stringify(envelope),
  });

  const responseEnvelope = await res.json();
  if (!res.ok) throw new Error(responseEnvelope.error ?? "Request failed");
  return decryptJson<TResponse>(session.aesKey, responseEnvelope);
}



export type { PageResponse };
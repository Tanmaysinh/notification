import { API_BASE_URL } from "@/lib/apiConfig";
import type { SecureSession } from "@/crypto/handshake";
import { encryptJson, decryptJson } from "@/crypto/cryptoService";
// import type {ReportFilters,ReportRow} from "@/types/report"

// interface PageResponse<T> {
//   content: T[];
//   totalElements: number;
//   totalPages: number;
//   number: number;
//   size: number;
// }

// function authHeaders(): HeadersInit {
//   const token = localStorage.getItem("token");
//   return token ? { Authorization: `Bearer ${token}` } : {};
// }

// async function securePost<TResponse>(
//   session: SecureSession,
//   path: string,
//   body: unknown
// ): Promise<TResponse> {
//   const envelope = await encryptJson(session.aesKey, body);

//   const res = await fetch(`${API_BASE_URL}${path}`, {
//     method: "POST",
//     headers: {
//       "Content-Type": "application/json",
//       "X-Session-Id": session.sessionId,
//       ...authHeaders(),
//     },
//     body: JSON.stringify(envelope),
//   });

//   const responseEnvelope = await res.json();
//   if (!res.ok) throw new Error(responseEnvelope.error ?? "Request failed");
//   return decryptJson<TResponse>(session.aesKey, responseEnvelope);
// }

// async function securePut<TResponse>(
//   session: SecureSession,
//   path: string,
//   body: unknown
// ): Promise<TResponse> {
//   const envelope = await encryptJson(session.aesKey, body);

//   const res = await fetch(`${API_BASE_URL}${path}`, {
//     method: "PUT",
//     headers: {
//       "Content-Type": "application/json",
//       "X-Session-Id": session.sessionId,
//       ...authHeaders(),
//     },
//     body: JSON.stringify(envelope),
//   });

//   const responseEnvelope = await res.json();
//   if (!res.ok) throw new Error(responseEnvelope.error ?? "Request failed");
//   return decryptJson<TResponse>(session.aesKey, responseEnvelope);
// }

// async function secureDelete<TResponse>(
//   session: SecureSession,
//   path: string
// ): Promise<TResponse> {
//   const res = await fetch(`${API_BASE_URL}${path}`, {
//     method: "DELETE",
//     headers: {
//       "X-Session-Id": session.sessionId,
//       ...authHeaders(),
//     },
//   });

//   const responseEnvelope = await res.json();
//   if (!res.ok) throw new Error(responseEnvelope.error ?? "Request failed");
//   return decryptJson<TResponse>(session.aesKey, responseEnvelope);
// }

// export async function contactList(
//   secureFetch: <T>(url: string, body: unknown) => Promise<T>,
//   params: { page: number; size: number; search?: string }
// ) {
//   return secureFetch<PageResponse<import("@/types/contact").Contact>>("/api/contacts/list", params);
// }

// export async function getPage<T>(
//   session: SecureSession,
//   basePath: string,
//   params: { page: number; size: number; search?: string }
// ): Promise<PageResponse<T>> {
//   return securePost<PageResponse<T>>(session, `${basePath}/list`, params);
// }

// export async function createItem<T>(session: SecureSession, path: string, body: unknown): Promise<T> {
//   return securePost<T>(session, path, body);
// }

// export async function updateItem<T>(session: SecureSession, path: string, id: string, body: unknown): Promise<T> {
//   return securePut<T>(session, `${path}/${id}`, body);
// }

// export async function deleteItem(session: SecureSession, path: string, id: string): Promise<void> {
//   await secureDelete<void>(session, `${path}/${id}`);
// }


// export async function getAllTemplates<T>(
//   session: SecureSession,
//   type: string
// ): Promise<T[]> {
//   // Reuses the existing paginated /list endpoint with a large page size —
//   // avoids needing a separate unpaginated backend route just for dropdowns.
//   const result = await getPage<T>(session, `/api/templates/${type}`, {
//     page: 0,
//     size: 1000,
//   });
//   return result.content;
// }

// export async function getAllCampaigns<T>(session: SecureSession): Promise<T[]> {
//   const result = await getPage<T>(session, "/api/campaigns", { page: 0, size: 1000 });
//   return result.content;
// }

// export async function sendNotification<TResponse>(
//   session: SecureSession,
//   body: unknown
// ): Promise<TResponse> {
//   return securePost<TResponse>(session, "/api/notifications/send", body);
// }

// export async function getReport(session: SecureSession, filters: ReportFilters) {
//   return securePost<PageResponse<ReportRow>>(session, "/api/report/list", filters);
// }

// export async function retryNotification(
//   session: SecureSession,
//   requestId: string,
//   contactId: string,
//   channelType: string
// ) {
//   return securePost<{ retried: boolean }>(session, "/api/report/retry", { requestId, contactId, channelType });
// }

// export type { PageResponse };




import type { ReportFilters, ReportRow } from "@/types/report";

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

type SecureFetch = <T>(url: string, body: unknown, init?: RequestInit) => Promise<T>;

export async function getPage<T>(
  secureFetch: SecureFetch,
  basePath: string,
  params: { page: number; size: number; search?: string }
): Promise<PageResponse<T>> {
  return secureFetch<PageResponse<T>>(`${basePath}/list`, params);
}

export async function createItem<T>(
  secureFetch: SecureFetch,
  path: string,
  body: unknown
): Promise<T> {
  return secureFetch<T>(path, body);
}

export async function updateItem<T>(
  secureFetch: SecureFetch,
  path: string,
  id: string,
  body: unknown
): Promise<T> {
  return secureFetch<T>(`${path}/${id}`, body, { method: "PUT" });
}

export async function deleteItem(
  secureFetch: SecureFetch,
  path: string,
  id: string
): Promise<void> {
  await secureFetch<void>(`${path}/${id}`, undefined, { method: "DELETE" });
}

export async function getAllTemplates<T>(
  secureFetch: SecureFetch,
  type: string
): Promise<T[]> {
  // Reuses the existing paginated /list endpoint with a large page size —
  // avoids needing a separate unpaginated backend route just for dropdowns.
  const result = await getPage<T>(secureFetch, `/api/templates/${type}`, {
    page: 0,
    size: 1000,
  });
  return result.content;
}

export async function getAllCampaigns<T>(secureFetch: SecureFetch): Promise<T[]> {
  const result = await getPage<T>(secureFetch, "/api/campaigns", { page: 0, size: 1000 });
  return result.content;
}

export async function sendNotification<TResponse>(
  secureFetch: SecureFetch,
  body: unknown
): Promise<TResponse> {
  return secureFetch<TResponse>("/api/notifications/send", body);
}

export async function getReport(
  secureFetch: SecureFetch,
  filters: ReportFilters
): Promise<PageResponse<ReportRow>> {
  return secureFetch<PageResponse<ReportRow>>("/api/report/list", filters);
}

export async function retryNotification(
  secureFetch: SecureFetch,
  requestId: string,
  contactId: string,
  channelType: string
): Promise<{ retried: boolean }> {
  return secureFetch<{ retried: boolean }>("/api/report/retry", { requestId, contactId, channelType });
}
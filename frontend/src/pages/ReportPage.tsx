import { useEffect, useState, useCallback } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
// import { getReport, retryNotification } from "@/lib/apiClient";
import type { ReportRow, ChannelRow,ReportFilters } from "@/types/report";
import Pagination from "@/components/Pagination";
import ChannelDetailModal from "@/components/ChannelDetailModal";
import { useSearchParams } from "react-router-dom";


const REQUEST_STATUSES = ["SCHEDULED", "PROCESSING", "COMPLETED"];
const NOTIFICATION_STATUSES = ["SENT", "FAILED", "PENDING"];
const NOTIFICATION_TYPES = ["sms", "email", "push"];
const CHANNEL_LABELS: Record<string, string> = { sms: "SMS", email: "Email", push: "Push" };
const GROUP_COLORS = ["bg-white/[0.02]", "bg-transparent"];
const PAGE_SIZE = 15;

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}


export default function ReportPage() {
  const { secureFetch } = useSecureSession();
  const [searchParams] = useSearchParams();
const requestIdFromUrl = searchParams.get("requestId");

  const [filters, setFilters] = useState<Omit<ReportFilters, "page" | "size">>({
    dateFrom: "",
    dateTo: "",
    requestId: requestIdFromUrl??"",
    notificationType: "",
    requestStatus: "",
    notificationStatus: "",
    contactSearch: "",
    campaignId: "",
  });

  const [page, setPage] = useState(0);
  const [rows, setRows] = useState<ReportRow[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [retryingKey, setRetryingKey] = useState<string | null>(null);
    const [expandedKey, setExpandedKey] = useState<string | null>(null);
  // const [detailChannel, setDetailChannel] = useState<{ row: ReportRow; channel: ChannelRow } | null>(null);
  const [detailChannel, setDetailChannel] = useState<ChannelRow | null>(null);
  const [retrying, setRetrying] = useState(false);


  function rowKey(row: ReportRow) {
    return `${row.requestId}:${row.contactId}`;
  }

  function toggleExpand(row: ReportRow) {
    const key = rowKey(row);
    setExpandedKey(expandedKey === key ? null : key);
  }

//   async function handleRetry() {
//     if (!detailChannel) return;
//     setRetrying(true);
//     try {
//       // const session = await getSession();
//       // await retryNotification(
//       //   session,
//       //   detailChannel.row.requestId,
//       //   detailChannel.row.contactId,
//       //   detailChannel.channel.channelType
//       // );

//       await secureFetch(
//   "/api/reports/retry",
//   {
//     requestId: detailChannel.row.requestId,
//     contactId: detailChannel.row.contactId,
//     channelType: detailChannel.channel.channelType,
//   }
// );
//       setDetailChannel(null);
//       load();
//     } catch (err: any) {
//       alert(err.message ?? "Retry failed.");
//     } finally {
//       setRetrying(false);
//     }
//   }

async function handleRetry(row: ReportRow, channel: ChannelRow) {
  const key = `${row.requestId}:${row.contactId}:${channel.channelType}`;
  setRetryingKey(key);
  try {
    await secureFetch("/api/report/retry", {
      requestId: row.requestId,
      contactId: row.contactId,
      channelType: channel.channelType,
    });
    load();
  } catch (err: any) {
    alert(err.message ?? "Retry failed.");
  } finally {
    setRetryingKey(null);
  }
}

  function overallStatusBadge(row: ReportRow) {
    const anyFailed = row.channels.some((c) => c.latestStatus === "FAILED");
    const anyPending = row.channels.some((c) => c.latestStatus === "PENDING");
    if (anyFailed) return { label: "Has failures", color: "bg-red-500/10 text-red-400" };
    if (anyPending) return { label: "In progress", color: "bg-yellow-500/10 text-yellow-400" };
    return { label: "All sent", color: "bg-green-500/10 text-green-400" };
  }

  const load = useCallback(async () => {
    setLoading(true);
    try {
      // const session = await getSession();
      const payload: ReportFilters = {
        ...filters,
        dateFrom: filters.dateFrom ? new Date(filters.dateFrom).toISOString() : null,
        dateTo: filters.dateTo ? new Date(filters.dateTo).toISOString() : null,
        page,
        size: PAGE_SIZE,
      };
      // const data = await getReport(session, payload);
      const data = await secureFetch<PageResponse<ReportRow>>(
        "/api/report/list",
        payload
      );

      setRows(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setRows([]);
    } finally {
      setLoading(false);
    }
  }, [filters, page, secureFetch]);

  useEffect(() => {
    load();
  }, [load]);

  function updateFilter<K extends keyof typeof filters>(key: K, value: string) {
    setFilters((f) => ({ ...f, [key]: value }));
  }

  function applyFilters() {
    setPage(0);
    load();
  }

//   async function handleRetry(row: ReportRow) {
//     const key = `${row.requestId}:${row.contactId}`;
//     setRetryingKey(key);
//     try {
//       // const session = await getSession();
//       // await retryNotification(session, row.requestId, row.contactId);
//       await secureFetch(
//   "/api/reports/retry",
//   {
//     requestId: detailChannel.row.requestId,
//     contactId: detailChannel.row.contactId,
//     channelType: detailChannel.channel.channelType,
//   }
// );
//       load();
//     } catch (err: any) {
//       alert(err.message ?? "Retry failed.");
//     } finally {
//       setRetryingKey(null);
//     }
//   }

  return (
    <div>
    <div className="mb-6">
        <h1 className="text-xl font-semibold text-white">Report</h1>
        <p className="text-sm text-gray-400 mt-0.5">Per-contact delivery status across all notification requests</p>
      </div>

      <div className="bg-[#171d2b] border border-white/10 rounded-xl p-4 mb-6 space-y-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div>
            <label className="text-xs text-gray-400 block mb-1">From date</label>
            <input
              type="date"
              value={filters.dateFrom ?? ""}
              onChange={(e) => updateFilter("dateFrom", e.target.value)}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63] [color-scheme:dark]"
            />
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">To date</label>
            <input
              type="date"
              value={filters.dateTo ?? ""}
              onChange={(e) => updateFilter("dateTo", e.target.value)}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63] [color-scheme:dark]"
            />
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Request ID</label>
            <input
              type="text"
              value={filters.requestId}
              onChange={(e) => updateFilter("requestId", e.target.value)}
              placeholder="Exact request id"
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Campaign ID</label>
            <input
              type="text"
              value={filters.campaignId}
              onChange={(e) => updateFilter("campaignId", e.target.value)}
              placeholder="Exact campaign id"
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div>
            <label className="text-xs text-gray-400 block mb-1">Notification Type</label>
            <select
              value={filters.notificationType}
              onChange={(e) => updateFilter("notificationType", e.target.value)}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            >
              <option value="">All</option>
              {NOTIFICATION_TYPES.map((t) => (
                <option key={t} value={t}>{t.toUpperCase()}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Request Status</label>
            <select
              value={filters.requestStatus}
              onChange={(e) => updateFilter("requestStatus", e.target.value)}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            >
              <option value="">All</option>
              {REQUEST_STATUSES.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Notification Status</label>
            <select
              value={filters.notificationStatus}
              onChange={(e) => updateFilter("notificationStatus", e.target.value)}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            >
              <option value="">All</option>
              {NOTIFICATION_STATUSES.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Search (phone or email)</label>
            <input
              type="text"
              value={filters.contactSearch}
              onChange={(e) => updateFilter("contactSearch", e.target.value)}
              placeholder="9876543210 or name@mail.com"
              className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>
        </div>

        <div className="flex justify-end">
          <button
            onClick={applyFilters}
            className="bg-[#2c4a63] text-white px-4 py-2 rounded-lg text-sm font-medium hover:opacity-90"
          >
            Apply Filters
          </button>
        </div>
      </div>
      

      <div className="bg-[#171d2b] border border-white/10 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/10 bg-white/5">
              <th className="text-left px-4 py-3 text-gray-400 font-medium w-8"></th>
              <th className="text-left px-4 py-3 text-gray-400 font-medium">Request ID</th>
              <th className="text-left px-4 py-3 text-gray-400 font-medium">Contact</th>
              <th className="text-left px-4 py-3 text-gray-400 font-medium">Campaign</th>
              <th className="text-left px-4 py-3 text-gray-400 font-medium">Scheduled</th>
              <th className="text-left px-4 py-3 text-gray-400 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="text-center py-8 text-gray-500">Loading…</td></tr>
            ) : rows.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-8 text-gray-500">No records found.</td></tr>
            ) : (
              rows.map((row, idx) => {
                const key = rowKey(row);
                const isExpanded = expandedKey === key;
                const badge = overallStatusBadge(row);
                const bg = GROUP_COLORS[idx % 2];

                console.log(row)

                return (
                  <>
                    <tr
                      key={key}
                      onClick={() => toggleExpand(row)}
                      className={`border-b border-white/5 cursor-pointer hover:bg-white/[0.04] ${bg}`}
                    >
                      <td className="px-4 py-3 text-gray-400">
                        <span className={`inline-block transition-transform ${isExpanded ? "rotate-90" : ""}`}>▸</span>
                      </td>
                      <td className="px-4 py-3 text-gray-300 font-mono text-xs">{row.requestId}</td>
                      <td className="px-4 py-3 text-gray-200">
                        <div>{row.contactId}</div>
                        {/* <div className="text-xs text-gray-500">{row.contactEmail ?? row.contactPhone}</div> */}
                      </td>
                      <td className="px-4 py-3 text-gray-300">{row.campaignName ?? "—"}</td>
                      <td className="px-4 py-3 text-gray-300 text-xs">
                        {row.scheduleTime ? new Date(row.scheduleTime).toLocaleString() : "Immediate"}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`text-xs px-2 py-1 rounded-md ${badge.color}`}>{badge.label}</span>
                      </td>
                    </tr>

                    {/* {isExpanded && (
                      <tr className={bg}>
                        <td></td>
                        <td colSpan={5} className="px-4 pb-4">
                          <div className="border border-white/10 rounded-lg overflow-hidden">
                            {row.channels.map((channel) => (
                              <div
                                key={channel.channelType}
                                className="flex items-center justify-between px-4 py-2.5 border-b border-white/5 last:border-0"
                              >
                                <div className="flex items-center gap-3">
                                  <span className="text-sm text-white font-medium w-20">
                                    {CHANNEL_LABELS[channel.channelType] ?? channel.channelType}
                                  </span>
                                  <span
                                    className={`text-xs px-2 py-1 rounded-md ${
                                      channel.latestStatus === "SENT"
                                        ? "bg-green-500/10 text-green-400"
                                        : channel.latestStatus === "FAILED"
                                        ? "bg-red-500/10 text-red-400"
                                        : "bg-yellow-500/10 text-yellow-400"
                                    }`}
                                  >
                                    {channel.latestStatus}
                                  </span>
                                  <span className="text-xs text-gray-500">Retries: {channel.retryCount}/3</span>
                                </div>

                                <button
                                  onClick={() => setDetailChannel({ row, channel })}
                                  className="px-3 py-1.5 rounded-lg text-xs border border-white/10 text-gray-300 hover:bg-white/5"
                                >
                                  Show Content
                                </button>
                              </div>
                            ))}
                          </div>
                        </td>
                      </tr>
                    )} */}

                    {isExpanded && (
  <tr className={bg}>
    <td></td>
    <td colSpan={5} className="px-4 pb-4">
      <div className="border border-white/10 rounded-lg overflow-hidden">
        {row.channels.map((channel) => {
          const retryKey = `${row.requestId}:${row.contactId}:${channel.channelType}`;
          return (
            <div
              key={channel.channelType}
              className="flex items-center justify-between px-4 py-2.5 border-b border-white/5 last:border-0"
            >
              <div className="flex items-center gap-3">
                <span className="text-sm text-white font-medium w-20">
                  {CHANNEL_LABELS[channel.channelType] ?? channel.channelType}
                </span>
                <span className="text-sm text-white font-medium w-80">
                  { channel.userData}
                </span>
                <span
                  className={`text-xs px-2 py-1 rounded-md ${
                    channel.latestStatus === "DELIVERED" || channel.latestStatus === "SENT"
                      ? "bg-green-500/10 text-green-400"
                      : channel.latestStatus === "FAILED"
                      ? "bg-red-500/10 text-red-400"
                      : "bg-yellow-500/10 text-yellow-400"
                  }`}
                >
                  {channel.latestStatus}
                </span>
                <span className="text-xs text-gray-500">Retries: {channel.retryCount}/3</span>
              </div>

              <div className="flex gap-2">
                {channel.retryEligible && (
                  <button
                    onClick={() => handleRetry(row, channel)}
                    disabled={retryingKey === retryKey}
                    className="px-3 py-1.5 rounded-lg text-xs border border-[#2c4a63] text-[#6b9bc4] hover:bg-[#2c4a63]/10 disabled:opacity-50"
                  >
                    {retryingKey === retryKey ? "Retrying…" : "Retry"}
                  </button>
                )}
                <button
                  onClick={() => setDetailChannel(channel)}
                  className="px-3 py-1.5 rounded-lg text-xs border border-white/10 text-gray-300 hover:bg-white/5"
                >
                  Show Content
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </td>
  </tr>
)}
                  </>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
{/* 
      <ChannelDetailModal
        open={!!detailChannel}
        channel={detailChannel?.channel ?? null}
        onClose={() => setDetailChannel(null)}
        onRetry={handleRetry}
        retrying={retrying}
      /> */}
      <ChannelDetailModal
        open={!!detailChannel}
        channel={detailChannel}
        onClose={() => setDetailChannel(null)}
      />
    </div>
    
  );
}
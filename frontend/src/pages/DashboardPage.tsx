import { useEffect, useState } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line,
} from "recharts";
import type { DashboardSummary } from "@/types/dashboard";

const STATUS_COLORS: Record<string, string> = {
  SCHEDULED: "#94a3b8",
  SENT: "#eab308",
  PROCESSING: "#3b82f6",
  COMPLETED: "#22c55e",
  DELIVERED: "#22c55e",
  FAILED: "#ef4444",
  PENDING: "#eab308",
  UNKNOWN: "#6b7280",
};

const CHANNEL_LABELS: Record<string, string> = { sms: "SMS", email: "Email", push: "Push" };

export default function DashboardPage() {
  const { secureFetch } = useSecureSession();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        // Using secureFetch generically as a GET-style call — if your
        // secureFetch always POSTs, pass an empty body; the backend
        // @GetMapping would need to become @PostMapping to match.
        const data = await secureFetch<DashboardSummary>("/api/dashboard/summary", {});
        setSummary(data);
      } catch {
        setSummary(null);
      } finally {
        setLoading(false);
      }
    })();
  }, [secureFetch]);

  if (loading) {
    return <p className="text-sm text-gray-400">Loading dashboard…</p>;
  }

  if (!summary) {
    return <p className="text-sm text-gray-400">Could not load dashboard data.</p>;
  }

  const statusPieData = Object.entries(summary.requestsByStatus).map(([status, count]) => ({
    name: status,
    value: count,
  }));

  const retryBarData = Object.entries(summary.retriesByChannel).map(([channel, count]) => ({
    channel: CHANNEL_LABELS[channel] ?? channel,
    retries: count,
  }));

  const channelKeys = Object.keys(summary.statusByChannel);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-white">Dashboard</h1>
        <p className="text-sm text-gray-400 mt-0.5">Overview of your notification activity</p>
      </div>

      {/* Top stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <StatCard label="Total Requests" value={summary.totalRequests} />
        <StatCard label="Total Recipients" value={summary.totalRecipients} />
        <StatCard
          label="Completed"
          value={summary.requestsByStatus["COMPLETED"] ?? 0}
        />
        <StatCard
          label="In Progress"
          value={(summary.requestsByStatus["PROCESSING"] ?? 0) + (summary.requestsByStatus["SENT"] ?? 0)}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Requests trend over time */}
        <div className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
          <h3 className="text-sm font-medium text-white mb-4">Requests — Last 14 Days</h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={summary.requestsOverTime}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="date" tick={{ fontSize: 10, fill: "#9ca3af" }} tickFormatter={(d) => d.slice(5)} />
              <YAxis tick={{ fontSize: 10, fill: "#9ca3af" }} allowDecimals={false} />
              <Tooltip
                contentStyle={{ background: "#0f1420", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8 }}
                labelStyle={{ color: "#fff" }}
              />
              <Line type="monotone" dataKey="count" stroke="#2c4a63" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Request status breakdown */}
        <div className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
          <h3 className="text-sm font-medium text-white mb-4">Requests by Status</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie
                data={statusPieData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={80}
                paddingAngle={2}
              >
                {statusPieData.map((entry) => (
                  <Cell key={entry.name} fill={STATUS_COLORS[entry.name] ?? "#6b7280"} />
                ))}
              </Pie>
              <Legend wrapperStyle={{ fontSize: 12, color: "#9ca3af" }} />
              <Tooltip
                contentStyle={{ background: "#0f1420", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8 }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Per-channel delivery status */}
      <div className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
        <h3 className="text-sm font-medium text-white mb-4">Delivery Status by Channel</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {channelKeys.map((channel) => {
            const statuses = summary.statusByChannel[channel];
            const data = Object.entries(statuses).map(([status, count]) => ({ status, count }));
            return (
              <div key={channel}>
                <p className="text-xs text-gray-400 mb-2">{CHANNEL_LABELS[channel] ?? channel}</p>
                <ResponsiveContainer width="100%" height={160}>
                  <BarChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                    <XAxis dataKey="status" tick={{ fontSize: 10, fill: "#9ca3af" }} />
                    <YAxis tick={{ fontSize: 10, fill: "#9ca3af" }} allowDecimals={false} />
                    <Tooltip
                      contentStyle={{ background: "#0f1420", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8 }}
                    />
                    <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                      {data.map((entry) => (
                        <Cell key={entry.status} fill={STATUS_COLORS[entry.status] ?? "#6b7280"} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            );
          })}
        </div>
      </div>

      {/* Retries by channel */}
      {retryBarData.length > 0 && (
        <div className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
          <h3 className="text-sm font-medium text-white mb-4">Total Retries by Channel</h3>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={retryBarData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis type="number" tick={{ fontSize: 10, fill: "#9ca3af" }} allowDecimals={false} />
              <YAxis type="category" dataKey="channel" tick={{ fontSize: 10, fill: "#9ca3af" }} width={60} />
              <Tooltip
                contentStyle={{ background: "#0f1420", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8 }}
              />
              <Bar dataKey="retries" fill="#ef4444" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
      <p className="text-sm text-gray-400">{label}</p>
      <p className="text-2xl font-semibold text-white mt-1">{value}</p>
    </div>
  );
}
export default function DashboardPage() {
  return (
    <div>
      <h1 className="text-xl font-semibold text-white mb-1">Dashboard</h1>
      <p className="text-sm text-gray-400 mb-6">Overview of your notification activity</p>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { label: "Total Sent", value: "0" },
          { label: "Active Campaigns", value: "0" },
          { label: "Contacts", value: "0" },
        ].map((stat) => (
          <div key={stat.label} className="bg-[#171d2b] border border-white/10 rounded-xl p-5">
            <p className="text-sm text-gray-400">{stat.label}</p>
            <p className="text-2xl font-semibold text-white mt-1">{stat.value}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
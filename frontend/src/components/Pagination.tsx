export default function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number; // 0-based
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i);
  const visible = pages.filter((p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1);

  return (
    <div className="flex items-center gap-1 justify-end mt-4">
      <button
        onClick={() => onPageChange(Math.max(0, page - 1))}
        disabled={page === 0}
        className="px-3 py-1.5 rounded-lg text-sm border border-white/10 text-gray-300 hover:bg-white/5 disabled:opacity-40"
      >
        Prev
      </button>

      {visible.map((p, idx) => (
        <span key={p} className="flex items-center">
          {idx > 0 && visible[idx - 1] !== p - 1 && (
            <span className="px-2 text-gray-500 text-sm">…</span>
          )}
          <button
            onClick={() => onPageChange(p)}
            className={`w-8 h-8 rounded-lg text-sm ${
              p === page ? "bg-[#2c4a63] text-white" : "text-gray-300 hover:bg-white/5"
            }`}
          >
            {p + 1}
          </button>
        </span>
      ))}

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
        disabled={page === totalPages - 1}
        className="px-3 py-1.5 rounded-lg text-sm border border-white/10 text-gray-300 hover:bg-white/5 disabled:opacity-40"
      >
        Next
      </button>
    </div>
  );
}
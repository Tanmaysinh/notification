import type { ReactNode } from "react";

export interface Column<T> {
  header: string;
  render: (row: T) => ReactNode;
}

export default function DataTable<T>({
  columns,
  rows,
  loading,
  onEdit,
  onDelete,
  emptyMessage = "No records found.",
}: {
  columns: Column<T>[];
  rows: T[];
  loading: boolean;
  onEdit: (row: T) => void;
  onDelete: (row: T) => void;
  emptyMessage?: string;
}) {
  return (
    <div className="bg-[#171d2b] border border-white/10 rounded-xl overflow-hidden">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-white/10 bg-white/5">
            {columns.map((col) => (
              <th key={col.header} className="text-left px-4 py-3 text-gray-400 font-medium">
                {col.header}
              </th>
            ))}
            <th className="text-right px-4 py-3 text-gray-400 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={columns.length + 1} className="text-center py-8 text-gray-500">
                Loading…
              </td>
            </tr>
          ) : rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length + 1} className="text-center py-8 text-gray-500">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            rows.map((row, idx) => (
              <tr key={idx} className="border-b border-white/5 last:border-0 hover:bg-white/[0.03]">
                {columns.map((col) => (
                  <td key={col.header} className="px-4 py-3 text-gray-200">
                    {col.render(row)}
                  </td>
                ))}
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => onEdit(row)}
                      className="px-3 py-1.5 rounded-lg text-xs border border-white/10 text-gray-300 hover:bg-white/5"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => onDelete(row)}
                      className="px-3 py-1.5 rounded-lg text-xs border border-red-500/30 text-red-400 hover:bg-red-500/10"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
import { useEffect, useState, useCallback } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
import { getPage } from "@/lib/apiClient";
import type { Contact } from "@/types/contact";

export default function ContactPicker({
  selected,
  onChange,
}: {
  selected: Contact[];
  onChange: (contacts: Contact[]) => void;
}) {
  const { getSession } = useSecureSession();
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Contact[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const search = useCallback(
    async (term: string) => {
      setLoading(true);
      try {
        const session = await getSession();
        const data = await getPage<Contact>(session, "/api/contacts", { page: 0, size: 20, search: term });
        setResults(data.content);
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    },
    [getSession]
  );

  useEffect(() => {
    const timeout = setTimeout(() => search(query), 300); // debounce
    return () => clearTimeout(timeout);
  }, [query, search]);

  function toggleContact(contact: Contact) {
    const exists = selected.some((c) => c.contactId === contact.contactId);
    if (exists) {
      onChange(selected.filter((c) => c.contactId !== contact.contactId));
    } else {
      onChange([...selected, contact]);
    }
  }

  return (
    <div className="relative">
      <label className="text-sm text-gray-400 block mb-1.5">Recipients</label>

      <div
        className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full cursor-text flex flex-wrap gap-1.5 min-h-[42px]"
        onClick={() => setOpen(true)}
      >
        {selected.map((c) => (
          <span
            key={c.contactId}
            className="bg-[#2c4a63] text-white text-xs px-2 py-1 rounded-md flex items-center gap-1.5"
          >
            {c.name}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                toggleContact(c);
              }}
              className="hover:text-red-300"
            >
              ×
            </button>
          </span>
        ))}
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setOpen(true)}
          placeholder={selected.length === 0 ? "Search contacts by name or email…" : ""}
          className="bg-transparent outline-none text-white text-sm flex-1 min-w-[120px]"
        />
      </div>

      {open && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />
          <div className="absolute z-20 mt-1 w-full bg-[#171d2b] border border-white/10 rounded-lg shadow-xl max-h-56 overflow-y-auto">
            {loading ? (
              <p className="text-xs text-gray-500 px-3 py-2.5">Searching…</p>
            ) : results.length === 0 ? (
              <p className="text-xs text-gray-500 px-3 py-2.5">No contacts found.</p>
            ) : (
              results.map((c) => {
                const isSelected = selected.some((s) => s.contactId === c.contactId);
                return (
                  <button
                    key={c.contactId}
                    type="button"
                    onClick={() => toggleContact(c)}
                    className={`w-full text-left px-3 py-2 text-sm hover:bg-white/5 flex items-center justify-between ${
                      isSelected ? "text-[#6b9bc4]" : "text-gray-200"
                    }`}
                  >
                    <span>
                      {c.name} <span className="text-gray-500">· {c.email}</span>
                    </span>
                    {isSelected && <span>✓</span>}
                  </button>
                );
              })
            )}
          </div>
        </>
      )}
    </div>
  );
}
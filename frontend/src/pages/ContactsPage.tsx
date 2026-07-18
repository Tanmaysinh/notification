import { useEffect, useState, useCallback } from "react";
import type { Contact, ContactFormValues } from "@/types/contact";
import { getPage, createItem, updateItem, deleteItem } from "@/lib/apiClient";
import DataTable, { type Column } from "@/components/DataTable";
import Pagination from "@/components/Pagination";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { useSecureSession } from "@/crypto/SecureSessionContext";

const PAGE_SIZE = 10;
const API_PATH = "/api/contacts";

const emptyForm: ContactFormValues = { name: "", email: "", phoneNumber: "" };

export default function ContactsPage() {
  const [rows, setRows] = useState<Contact[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Contact | null>(null);
  const [form, setForm] = useState<ContactFormValues>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<Contact | null>(null);
  const [deleting, setDeleting] = useState(false);

//   const load = useCallback(async () => {
//     setLoading(true);
//     try {
//       const data = await getPage<Contact>(API_PATH, { page, size: PAGE_SIZE, search });
//       setRows(data.content);
//       setTotalPages(data.totalPages);
//     } catch {
//       setRows([]);
//     } finally {
//       setLoading(false);
//     }
//   }, [page, search]);

const { getSession } = useSecureSession();
const load = useCallback(async () => {
  setLoading(true);
  try {
    const session = await getSession();
    const data = await getPage<Contact>(session, API_PATH, { page, size: PAGE_SIZE, search });
    setRows(data.content);
    setTotalPages(data.totalPages);
  } catch {
    setRows([]);
  } finally {
    setLoading(false);
  }
}, [ page, search]);

  useEffect(() => {
    load();
  }, [load]);

  // reset to page 0 whenever the search changes
  useEffect(() => {
    setPage(0);
  }, [search]);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(contact: Contact) {
    setEditing(contact);
    setForm({ name: contact.name, email: contact.email, phoneNumber: contact.phoneNumber });
    setFormError(null);
    setModalOpen(true);
  }

//   async function handleSave(e: React.FormEvent) {
//     e.preventDefault();
//     setSaving(true);
//     setFormError(null);
//     try {
//       if (editing) {
//         await updateItem<Contact>(API_PATH, editing.contactId, form);
//       } else {
//         await createItem<Contact>(API_PATH, form);
//       }
//       setModalOpen(false);
//       load();
//     } catch (err: any) {
//       setFormError(err.message ?? "Could not save contact.");
//     } finally {
//       setSaving(false);
//     }
//   }

async function handleSave(e: React.FormEvent) {
  e.preventDefault();
  setSaving(true);
  setFormError(null);
  try {
    const session = await getSession();
    if (editing) {
      await updateItem<Contact>(session, API_PATH, editing.contactId, form);
    } else {
      await createItem<Contact>(session, API_PATH, form);
    }
    setModalOpen(false);
    load();
  } catch (err: any) {
    setFormError(err.message ?? "Could not save template.");
  } finally {
    setSaving(false);
  }
}

//   async function handleDelete() {
//     if (!deleteTarget) return;
//     setDeleting(true);
//     try {
//       await deleteItem(API_PATH, deleteTarget.contactId);
//       setDeleteTarget(null);
//       load();
//     } finally {
//       setDeleting(false);
//     }
//   }

async function handleDelete() {
  if (!deleteTarget) return;
  setDeleting(true);
  try {
    const session = await getSession();
    await deleteItem(session, API_PATH, deleteTarget.contactId);
    setDeleteTarget(null);
    load();
  } finally {
    setDeleting(false);
  }
}

  const columns: Column<Contact>[] = [
    { header: "Name", render: (r) => r.name },
    { header: "Email", render: (r) => r.email },
    { header: "Phone", render: (r) => r.phoneNumber },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-white">Contacts</h1>
          <p className="text-sm text-gray-400 mt-0.5">Manage your notification recipients</p>
        </div>
        <button
          onClick={openCreate}
          className="bg-[#2c4a63] text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:opacity-90"
        >
          + Add Contact
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search by name or email…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="bg-[#171d2b] border border-white/10 rounded-lg px-4 py-2.5 w-full max-w-sm outline-none text-white placeholder-gray-500 focus:border-[#2c4a63] text-sm"
        />
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        onEdit={openEdit}
        onDelete={setDeleteTarget}
        emptyMessage="No contacts yet. Add your first one."
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal
        open={modalOpen}
        title={editing ? "Edit Contact" : "Add Contact"}
        onClose={() => setModalOpen(false)}
      >
        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="text-sm text-gray-400 block mb-1.5">Name</label>
            <input
              type="text"
              required
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>
          <div>
            <label className="text-sm text-gray-400 block mb-1.5">Email</label>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>
          <div>
            <label className="text-sm text-gray-400 block mb-1.5">Phone Number</label>
            <input
              type="tel"
              required
              value={form.phoneNumber}
              onChange={(e) => setForm((f) => ({ ...f, phoneNumber: e.target.value }))}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            />
          </div>

          {formError && <p className="text-sm text-red-400">{formError}</p>}

          <div className="flex gap-3 justify-end pt-2">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 rounded-lg text-sm border border-white/10 text-gray-300 hover:bg-white/5"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-2 rounded-lg text-sm bg-[#2c4a63] text-white hover:opacity-90 disabled:opacity-50"
            >
              {saving ? "Saving…" : editing ? "Save Changes" : "Add Contact"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete contact?"
        message={`This will permanently remove ${deleteTarget?.name ?? "this contact"}.`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        confirming={deleting}
      />
    </div>
  );
}
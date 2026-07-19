import { useEffect, useState, useCallback } from "react";
import type { Template, TemplateFormValues, TemplateType } from "@/types/template";
// import { getPage, createItem, updateItem, deleteItem } from "@/lib/apiClient";
import DataTable, { type Column } from "@/components/DataTable";
import Pagination from "@/components/Pagination";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { useSecureSession } from "@/crypto/SecureSessionContext";

const PAGE_SIZE = 10;

const emptyForm: TemplateFormValues = { name: "", content: "" };



export default function TemplateManager({
  type,
  title,
}: {
  type: TemplateType;
  title: string;
}) {
  const apiPath = `/api/templates/${type}`;

  const [rows, setRows] = useState<Template[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Template | null>(null);
  const [form, setForm] = useState<TemplateFormValues>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<Template | null>(null);
  const [deleting, setDeleting] = useState(false);
const { secureFetch } = useSecureSession();

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

const load = useCallback(async () => {
  setLoading(true);

  try {
    const data = await secureFetch<PageResponse<Template>>(
      `${apiPath}/list`,
      {
        page,
        size: PAGE_SIZE,
        search,
      }
    );

    setRows(data.content);
    setTotalPages(data.totalPages);
  } catch {
    setRows([]);
  } finally {
    setLoading(false);
  }
}, [apiPath, page, search, secureFetch]);

//   const load = useCallback(async () => {
//     setLoading(true);
//     try {
//       const data = await getPage<Template>(apiPath, { page, size: PAGE_SIZE, search });
//       setRows(data.content);
//       setTotalPages(data.totalPages);
//     } catch {
//       setRows([]);
//     } finally {
//       setLoading(false);
//     }
//   }, [apiPath, page, search]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [search, type]);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(template: Template) {
    setEditing(template);
    setForm({ name: template.name, content: template.content });
    setFormError(null);
    setModalOpen(true);
  }

//   async function handleSave(e: React.FormEvent) {
//     e.preventDefault();
//     setSaving(true);
//     setFormError(null);
//     try {
//       if (editing) {
//         await updateItem<Template>(apiPath, editing.templateId, form);
//       } else {
//         await createItem<Template>(apiPath, form);
//       }
//       setModalOpen(false);
//       load();
//     } catch (err: any) {
//       setFormError(err.message ?? "Could not save template.");
//     } finally {
//       setSaving(false);
//     }
//   }

async function handleSave(e: React.FormEvent) {
  e.preventDefault();

  setSaving(true);
  setFormError(null);

  try {
    if (editing) {
      await secureFetch<Template>(
        `${apiPath}/${editing.templateId}`,
        form,
        {
          method: "PUT",
        }
      );
    } else {
      await secureFetch<Template>(
        apiPath,
        form
      );
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
//       await deleteItem(apiPath, deleteTarget.templateId);
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
    await secureFetch(
      `${apiPath}/${deleteTarget.templateId}`,
      undefined,
      {
        method: "DELETE",
      }
    );

    setDeleteTarget(null);
    load();
  } finally {
    setDeleting(false);
  }
}

  const columns: Column<Template>[] = [
    { header: "Name", render: (r) => r.name },
    {
      header: "Content",
      render: (r) => (
        <span className="line-clamp-1 max-w-md inline-block align-bottom">{r.content}</span>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-white">{title}</h1>
          <p className="text-sm text-gray-400 mt-0.5">Manage your {type} templates</p>
        </div>
        <button
          onClick={openCreate}
          className="bg-[#2c4a63] text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:opacity-90"
        >
          + Add Template
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search by name…"
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
        emptyMessage={`No ${type} templates yet. Add your first one.`}
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal
        open={modalOpen}
        title={editing ? "Edit Template" : "Add Template"}
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
            <label className="text-sm text-gray-400 block mb-1.5">Content</label>
            <textarea
              required
              rows={5}
              value={form.content}
              onChange={(e) => setForm((f) => ({ ...f, content: e.target.value }))}
              className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63] resize-none"
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
              {saving ? "Saving…" : editing ? "Save Changes" : "Add Template"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete template?"
        message={`This will permanently remove "${deleteTarget?.name ?? ""}".`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        confirming={deleting}
      />
    </div>
  );
}
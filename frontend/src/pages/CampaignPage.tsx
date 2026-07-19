import { useEffect, useState, useCallback } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
// import { getPage, createItem, updateItem, deleteItem } from "@/lib/apiClient";
import type { Campaign, CampaignFormValues } from "@/types/campaign";
import DataTable, { type Column } from "@/components/DataTable";
import Pagination from "@/components/Pagination";
import ConfirmDialog from "@/components/ConfirmDialog";
import CampaignFormModal from "@/components/CampaignFormModal";

const PAGE_SIZE = 10;
const API_PATH = "/api/campaigns";

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default function CampaignPage() {
  // const { getSession } = useSecureSession();
  const { secureFetch } = useSecureSession();

  const [rows, setRows] = useState<Campaign[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Campaign | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<Campaign | null>(null);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await secureFetch<PageResponse<Campaign>>(
      `${API_PATH}/list`,
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
  }, [page, search, secureFetch]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [search]);

  function openCreate() {
    setEditing(null);
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(campaign: Campaign) {
    setEditing(campaign);
    setFormError(null);
    setModalOpen(true);
  }

  async function handleSave(_name: string, values: CampaignFormValues) {
    setSaving(true);
    setFormError(null);
    try {
      // const session = await getSession();
      // if (editing) {
      //   await updateItem<Campaign>(session, API_PATH, editing.campaignId, values);
      // } else {
      //   await createItem<Campaign>(session, API_PATH, values);
      // }
      if (editing) {
  await secureFetch<Campaign>(
    `${API_PATH}/${editing.campaignId}`,
    values,
    {
      method: "PUT",
    }
  );
} else {
  await secureFetch<Campaign>(
    API_PATH,
    values
  );
}
      setModalOpen(false);
      load();
    } catch (err: any) {
      setFormError(err.message ?? "Could not save campaign.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      // const session = await getSession();
      // await deleteItem(session, API_PATH, deleteTarget.campaignId);
      await secureFetch(
  `${API_PATH}/${deleteTarget.campaignId}`,
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

  const columns: Column<Campaign>[] = [
    { header: "Campaign Name", render: (r) => r.name },
    { header: "SMS Template", render: (r) => r.smsTemplateName ?? "—" },
    { header: "Email Template", render: (r) => r.emailTemplateName ?? "—" },
    { header: "Push Template", render: (r) => r.pushTemplateName ?? "—" },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-white">Campaigns</h1>
          <p className="text-sm text-gray-400 mt-0.5">
            Combine SMS, email, and push templates into one campaign
          </p>
        </div>
        <button
          onClick={openCreate}
          className="bg-[#2c4a63] text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:opacity-90"
        >
          + Create Campaign
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search campaigns…"
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
        emptyMessage="No campaigns yet. Create your first one."
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <CampaignFormModal
        open={modalOpen}
        editing={editing}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSave}
        submitting={saving}
        submitError={formError}
        title="Campaign"
      />

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete campaign?"
        message={`This will permanently remove "${deleteTarget?.name ?? ""}".`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        confirming={deleting}
      />
    </div>
  );
}
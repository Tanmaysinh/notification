import { useEffect, useState } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";
// import { getAllCampaigns, sendNotification } from "@/lib/apiClient";
import { useChannelSelection } from "@/types/useChannelSelection";
import ChannelPicker from "@/components/ChannelPicker";
import PreviewPanel from "@/components/PreviewPanel";
import ContactPicker from "@/components/ContactPicker";
import SendResultModal from "@/components/SendResultModal";
import type { Campaign } from "@/types/campaign";
import type { Contact } from "@/types/contact";
import type { SendNotificationResponse } from "@/types/notification";
import type {  TemplateType } from "@/types/template";

type Mode = "campaign" | "custom";

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}


export default function SendNotificationPage() {
  // const { getSession } = useSecureSession();
  const { secureFetch } = useSecureSession();

  const [mode, setMode] = useState<Mode>("campaign");
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [campaignsLoading, setCampaignsLoading] = useState(true);
  const [selectedCampaignId, setSelectedCampaignId] = useState<string>("");

  const [selectedContacts, setSelectedContacts] = useState<Contact[]>([]);

  const {
    channels,
    setChannels,
    activePreview,
    setActivePreview,
    ensureTemplatesLoaded,
    toggleChannel,
    selectTemplate,
    reset,
  } = useChannelSelection();

  const [sending, setSending] = useState(false);
  const [sendError, setSendError] = useState<string | null>(null);
  const [resultRequestId, setResultRequestId] = useState<string | null>(null);

  const [scheduleTime, setScheduleTime] = useState<string>(""); // datetime-local value

  // Load campaigns on mount
  useEffect(() => {
    (async () => {
      setCampaignsLoading(true);
      try {
        // const session = await getSession();
        // const data = await getAllCampaigns<Campaign>(session);

        const data = await secureFetch<PageResponse<Campaign>>("/api/campaigns/list", { page: 0, size: 1000 });


        // export async function getAllCampaigns<T>(secureFetch: SecureFetch): Promise<T[]> {
        //   const result = await getPage<T>(secureFetch, "/api/campaigns", { page: 0, size: 1000 });
        //   return result.content;
        // }
        setCampaigns(data.content);
      } catch {
        setCampaigns([]);
      } finally {
        setCampaignsLoading(false);
      }
    })();
  }, [secureFetch]);

  function switchMode(next: Mode) {
    setMode(next);
    setSendError(null);
    reset();
    setSelectedCampaignId("");
  }

  // When a campaign is picked, populate channel/template preview state to mirror it (read-only preview)
  async function handleSelectCampaign(campaignId: string) {
    setSelectedCampaignId(campaignId);
    const campaign = campaigns.find((c) => c.campaignId === campaignId);
    if (!campaign) return;

    reset();
    const updates: TemplateType[] = [];
    if (campaign.smsTemplateId) updates.push("sms");
    if (campaign.emailTemplateId) updates.push("email");
    if (campaign.pushTemplateId) updates.push("push");

    for (const type of updates) {
      await ensureTemplatesLoaded(type);
    }

    setChannels((c) => {
      const next = { ...c };
      if (campaign.smsTemplateId) next.sms = { ...next.sms, enabled: true, templateId: campaign.smsTemplateId };
      if (campaign.emailTemplateId) next.email = { ...next.email, enabled: true, templateId: campaign.emailTemplateId };
      if (campaign.pushTemplateId) next.push = { ...next.push, enabled: true, templateId: campaign.pushTemplateId };
      return next;
    });

    setActivePreview(updates[0] ?? "sms");
  }

  async function handleSend() {
    setSendError(null);

    if (selectedContacts.length === 0) {
      setSendError("Select at least one recipient.");
      return;
    }

    const anyChannelEnabled = channels.sms.enabled || channels.email.enabled || channels.push.enabled;
    if (mode === "custom" && !anyChannelEnabled) {
      setSendError("Enable at least one channel.");
      return;
    }
    if (mode === "campaign" && !selectedCampaignId) {
      setSendError("Select a campaign.");
      return;
    }

    setSending(true);
    try {
      // const session = await getSession();
    //   const payload =
    //     mode === "campaign"
    //       ? { campaignId: selectedCampaignId, contactIds: selectedContacts.map((c) => c.contactId) }
    //       : {
    //           campaignId: null,
    //           smsTemplateId: channels.sms.enabled ? channels.sms.templateId : null,
    //           emailTemplateId: channels.email.enabled ? channels.email.templateId : null,
    //           pushTemplateId: channels.push.enabled ? channels.push.templateId : null,
    //           contactIds: selectedContacts.map((c) => c.contactId),
    //         };

    const payload =
        mode === "campaign"
            ? {
                campaignId: selectedCampaignId,
                contactIds: selectedContacts.map((c) => c.contactId),
                scheduleTime: scheduleTime ? new Date(scheduleTime).toISOString() : null,
            }
            : {
                campaignId: null,
                smsTemplateId: channels.sms.enabled ? channels.sms.templateId : null,
                emailTemplateId: channels.email.enabled ? channels.email.templateId : null,
                pushTemplateId: channels.push.enabled ? channels.push.templateId : null,
                contactIds: selectedContacts.map((c) => c.contactId),
                scheduleTime: scheduleTime ? new Date(scheduleTime).toISOString() : null,
            };


          

      // const result = await sendNotification<SendNotificationResponse>(session, payload);
      const result = await secureFetch<SendNotificationResponse>(
  "/api/notifications/send",
  payload
);
      setResultRequestId(result.requestId);
    } catch (err: any) {
      setSendError(err.message ?? "Could not send notification.");
    } finally {
      setSending(false);
    }
  }

  function handleCloseResult() {
    setResultRequestId(null);
    setSelectedContacts([]);
    reset();
    setSelectedCampaignId("");
    setMode("campaign");
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-xl font-semibold text-white">Send Notification</h1>
        <p className="text-sm text-gray-400 mt-0.5">
          Trigger a notification using a saved campaign or a custom one-off configuration
        </p>
      </div>

      <div className="flex mb-6 border border-white/10 rounded-lg p-1 bg-[#171d2b] max-w-xs">
        <button
          type="button"
          onClick={() => switchMode("campaign")}
          className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
            mode === "campaign" ? "bg-[#2c4a63] text-white" : "text-gray-400 hover:text-gray-200"
          }`}
        >
          Use Campaign
        </button>
        <button
          type="button"
          onClick={() => switchMode("custom")}
          className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
            mode === "custom" ? "bg-[#2c4a63] text-white" : "text-gray-400 hover:text-gray-200"
          }`}
        >
          Custom
        </button>
      </div>

      <div className="bg-[#171d2b] border border-white/10 rounded-2xl p-6 space-y-6 max-w-3xl">
        {mode === "campaign" ? (
          <div>
            <label className="text-sm text-gray-400 block mb-1.5">Select Campaign</label>
            {campaignsLoading ? (
              <p className="text-xs text-gray-500">Loading campaigns…</p>
            ) : campaigns.length === 0 ? (
              <p className="text-xs text-gray-500">No campaigns yet. Create one first.</p>
            ) : (
              <select
                value={selectedCampaignId}
                onChange={(e) => handleSelectCampaign(e.target.value)}
                className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
              >
                <option value="" disabled>
                  Choose a campaign
                </option>
                {campaigns.map((c) => (
                  <option key={c.campaignId} value={c.campaignId}>
                    {c.name}
                  </option>
                ))}
              </select>
            )}
          </div>
        ) : null}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <div className="space-y-5">
            {mode === "custom" && (
              <ChannelPicker
                channels={channels}
                onToggle={toggleChannel}
                onSelectTemplate={selectTemplate}
                onFocusChannel={setActivePreview}
              />
            )}
            <ContactPicker selected={selectedContacts} onChange={setSelectedContacts} />
          </div>

          <PreviewPanel channels={channels} activePreview={activePreview} onSelectPreview={setActivePreview} />
        </div>

        <div>
            <label className="text-sm text-gray-400 block mb-1.5">Schedule (optional)</label>
            <input
                type="datetime-local"
                value={scheduleTime}
                onChange={(e) => setScheduleTime(e.target.value)}
                min={new Date().toISOString().slice(0, 16)}
                className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63] [color-scheme:dark]"
            />
            <p className="text-xs text-gray-500 mt-1">Leave empty to send immediately.</p>
        </div>

        {sendError && (
          <div className="bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-2.5">
            <p className="text-sm text-red-400">{sendError}</p>
          </div>
        )}

        <div className="flex justify-end pt-2">
          <button
            onClick={handleSend}
            disabled={sending}
            className="px-5 py-2.5 rounded-lg text-sm bg-[#2c4a63] text-white font-medium hover:opacity-90 disabled:opacity-50"
          >
            {sending ? "Sending…" : "Send Notification"}
          </button>
        </div>
      </div>

      <SendResultModal open={!!resultRequestId} requestId={resultRequestId} onClose={handleCloseResult} />
    </div>
  );
} 
import { useEffect, useState } from "react";
import Modal from "@/components/Modal";
import ChannelPreview from "@/components/previews/ChannelPreview";
import { useSecureSession } from "@/crypto/SecureSessionContext";
// import { getAllTemplates } from "@/lib/apiClient";
import type { Template, TemplateType } from "@/types/template";
import type { Campaign, CampaignFormValues } from "@/types/campaign";

const CHANNELS: { type: TemplateType; label: string }[] = [
  { type: "sms", label: "SMS" },
  { type: "email", label: "Email" },
  { type: "push", label: "Push" },
];

interface ChannelState {
  enabled: boolean;
  templateId: string | null;
  templates: Template[];
  loading: boolean;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

function initChannelState(): Record<TemplateType, ChannelState> {
  return {
    sms: { enabled: false, templateId: null, templates: [], loading: false },
    email: { enabled: false, templateId: null, templates: [], loading: false },
    push: { enabled: false, templateId: null, templates: [], loading: false },
  };
}

export default function CampaignFormModal({
  open,
  editing,
  onClose,
  onSubmit,
  submitting,
  submitError,
  title = "Campaign",
}: {
  open: boolean;
  editing: Campaign | null;
  onClose: () => void;
  onSubmit: (name: string, values: CampaignFormValues) => void;
  submitting: boolean;
  submitError: string | null;
  title?: string;
}) {
  const { secureFetch } = useSecureSession();
  const [name, setName] = useState("");
  const [channels, setChannels] = useState<Record<TemplateType, ChannelState>>(initChannelState());
  const [activePreview, setActivePreview] = useState<TemplateType>("sms");
  const [errors, setErrors] = useState<Partial<Record<TemplateType, string>>>({});



  // Load templates for a channel the first time it's enabled
  async function ensureTemplatesLoaded(type: TemplateType) {
    if (channels[type].templates.length > 0 || channels[type].loading) return;

    setChannels((c) => ({ ...c, [type]: { ...c[type], loading: true } }));
    try {
    const response = await secureFetch<PageResponse<Template>>(
  `/api/templates/${type}/list`,{
    page: 0,
    size: 1000,
    search:""
  }
);

console.log(response)

setChannels((c) => ({
  ...c,
  [type]: {
    ...c[type],
    templates: response.content,
    loading: false,
  },
}));

      // setChannels((c) => ({
      //   ...c,
      //   [type]: {
      //     ...c[type],
      //     templates: response,
      //     loading: false,
      //   },
      // }));
      // setChannels((c) => ({ ...c, [type]: { ...c[type], templates, loading: false } }));
    } catch {
      setChannels((c) => ({ ...c, [type]: { ...c[type], loading: false } }));
    }
  }

  function toggleChannel(type: TemplateType) {
    const nextEnabled = !channels[type].enabled;
    setChannels((c) => ({
      ...c,
      [type]: { ...c[type], enabled: nextEnabled, templateId: nextEnabled ? c[type].templateId : null },
    }));
    if (nextEnabled) {
      ensureTemplatesLoaded(type);
      setActivePreview(type);
    }
  }

  function selectTemplate(type: TemplateType, templateId: string) {
    setChannels((c) => ({ ...c, [type]: { ...c[type], templateId } }));
    setActivePreview(type);
  }

  // Reset / prefill whenever the modal opens
  useEffect(() => {
    if (!open) return;

    if (editing) {
      setName(editing.name);
      const next = initChannelState();
      if (editing.smsTemplateId) next.sms = { ...next.sms, enabled: true, templateId: editing.smsTemplateId };
      if (editing.emailTemplateId) next.email = { ...next.email, enabled: true, templateId: editing.emailTemplateId };
      if (editing.pushTemplateId) next.push = { ...next.push, enabled: true, templateId: editing.pushTemplateId };
      setChannels(next);

      ["sms", "email", "push"].forEach((t) => {
        const type = t as TemplateType;
        if (next[type].enabled) ensureTemplatesLoaded(type);
      });

      const firstEnabled = CHANNELS.find((c) => next[c.type].enabled)?.type ?? "sms";
      setActivePreview(firstEnabled);
    } else {
      setName("");
      setChannels(initChannelState());
      setActivePreview("sms");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, editing]);

//   function handleSubmit(e: React.FormEvent) {
//     e.preventDefault();
//     onSubmit(name, {
//       name,
//       smsTemplateId: channels.sms.enabled ? channels.sms.templateId : null,
//       emailTemplateId: channels.email.enabled ? channels.email.templateId : null,
//       pushTemplateId: channels.push.enabled ? channels.push.templateId : null,
//     });
//   }

function handleSubmit(e: React.FormEvent) {
  e.preventDefault();
  const newErrors: Partial<Record<TemplateType, string>> = {};

  if (channels.sms.enabled && !channels.sms.templateId) {
    newErrors.sms = "Please select an SMS template.";
    // return alert("Please select an SMS template.");
  }

  if (channels.email.enabled && !channels.email.templateId) {
    newErrors.email = "Please select an Email template.";
    // return alert("Please select an Email template.");
  }

  if (channels.push.enabled && !channels.push.templateId) {
    newErrors.push = "Please select a Push Notification template.";
    // return alert("Please select a Push Notification template.");
  }

  setErrors(newErrors);

  if (Object.keys(newErrors).length > 0) {
    return;
  }


  onSubmit(name, {
    name,
    smsTemplateId: channels.sms.enabled
      ? channels.sms.templateId
      : null,
    emailTemplateId: channels.email.enabled
      ? channels.email.templateId
      : null,
    pushTemplateId: channels.push.enabled
      ? channels.push.templateId
      : null,
  });
}

  const previewTemplate = channels[activePreview].templates.find(
    (t) => t.templateId === channels[activePreview].templateId
  );

  const anyEnabled = channels.sms.enabled || channels.email.enabled || channels.push.enabled;

  return (
    <Modal open={open} title={editing ? `Edit ${title}` : `Create ${title}`} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="text-sm text-gray-400 block mb-1.5">{title} Name</label>
          <input
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
            placeholder={`e.g. Diwali ${title}`}
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {/* Left: channel toggles + template dropdowns */}
          <div className="space-y-3">
            {CHANNELS.map(({ type, label }) => {
              const channel = channels[type];
              return (
                <div
                  key={type}
                  className={`border rounded-lg p-3 transition ${
                    channel.enabled ? "border-[#2c4a63] bg-[#2c4a63]/10" : "border-white/10"
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-white font-medium">{label}</span>
                    <button
                      type="button"
                      onClick={() => toggleChannel(type)}
                      className={`relative w-10 h-5.5 h-6 rounded-full transition ${
                        channel.enabled ? "bg-[#2c4a63]" : "bg-white/10"
                      }`}
                    >
                      <span
                        className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform ${
                          channel.enabled ? "translate-x-4" : ""
                        }`}
                      />
                    </button>
                  </div>

                  {channel.enabled && (
                    <div className="mt-2.5">
                      {channel.loading ? (
                        <p className="text-xs text-gray-500">Loading templates…</p>
                      ) : channel.templates.length === 0 ? (
                        <p className="text-xs text-gray-500">
                          No {label.toLowerCase()} templates yet. Create one first.
                        </p>
                      ) : (
                        <select
                          value={channel.templateId ?? ""}
                          onChange={(e) => selectTemplate(type, e.target.value)}
                          onFocus={() => setActivePreview(type)}
                        //   className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
                        className={`bg-[#0f1420] rounded-lg px-3 py-2 w-full outline-none text-white text-sm ${
      errors[type]
        ? "border border-red-500"
        : "border border-white/10 focus:border-[#2c4a63]"
    }`}
                        >
                          <option value="" disabled>
                            Select a template
                          </option>
                          {channel.templates.map((t) => (
                            <option key={t.templateId} value={t.templateId}>
                              {t.name}
                            </option>
                          ))}
                        </select>
                        
                      )}
                      {errors[type] && (
    <p className="mt-1 text-xs text-red-400">
      {errors[type]}
    </p>
  )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Right: live preview */}
          <div className="bg-black/20 rounded-lg p-4 flex flex-col">
            {anyEnabled && (
            //   <div className="flex gap-1.5 mb-4 justify-center">
                <div className="grid grid-cols-3 gap-2 mb-4 w-full"> 
                {CHANNELS.filter((c) => channels[c.type].enabled).map((c) => (
                //   <button
                //     key={c.type}
                //     type="button"
                //     onClick={() => setActivePreview(c.type)}
                //     className={`px-3 py-1 rounded-md text-xs font-medium transition ${
                //       activePreview === c.type
                //         ? "bg-[#2c4a63] text-white"
                //         : "text-gray-400 hover:text-gray-200"
                //     }`}
                //   >
                <button
  key={c.type}
  type="button"
  onClick={() => setActivePreview(c.type)}
  className={`rounded-md px-2 py-2 text-sm font-medium transition w-full
    ${
      activePreview === c.type
        ? "bg-[#2c4a63] text-white"
        : "text-gray-400 hover:bg-white/5"
    }`}
>
  {c.label}
</button>
                ))}
              </div>
            )}

            <div className="flex-1 flex items-center justify-center">
              {!anyEnabled ? (
                <p className="text-sm text-gray-500 text-center">
                  Enable a channel to see a live preview here.
                </p>
              ) : !previewTemplate ? (
                <p className="text-sm text-gray-500 text-center">Select a template to preview it.</p>
              ) : (
                <ChannelPreview
                  type={activePreview}
                  name={previewTemplate.name}
                  content={previewTemplate.content}
                />
              )}
            </div>
          </div>
        </div>

        {submitError && <p className="text-sm text-red-400">{submitError}</p>}

        <div className="flex gap-3 justify-end pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 rounded-lg text-sm border border-white/10 text-gray-300 hover:bg-white/5"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting || !anyEnabled}
            className="px-4 py-2 rounded-lg text-sm bg-[#2c4a63] text-white hover:opacity-90 disabled:opacity-50"
          >
            {submitting ? "Saving…" : editing ? "Save Changes" : `Create ${title}`}
          </button>
        </div>
      </form>
    </Modal>
  );
}
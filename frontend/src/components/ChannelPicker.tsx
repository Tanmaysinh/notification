import type { TemplateType } from "@/types/template";
import type { ChannelState } from "@/types/useChannelSelection";

const CHANNELS: { type: TemplateType; label: string }[] = [
  { type: "sms", label: "SMS" },
  { type: "email", label: "Email" },
  { type: "push", label: "Push Notification" },
];

export default function ChannelPicker({
  channels,
  onToggle,
  onSelectTemplate,
  onFocusChannel,
}: {
  channels: Record<TemplateType, ChannelState>;
  onToggle: (type: TemplateType) => void;
  onSelectTemplate: (type: TemplateType, templateId: string) => void;
  onFocusChannel: (type: TemplateType) => void;
}) {
  return (
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
                onClick={() => onToggle(type)}
                className={`relative w-10 h-6 rounded-full transition ${
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
                    onChange={(e) => onSelectTemplate(type, e.target.value)}
                    onFocus={() => onFocusChannel(type)}
                    className="bg-[#0f1420] border border-white/10 rounded-lg px-3 py-2 w-full outline-none text-white text-sm focus:border-[#2c4a63]"
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
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
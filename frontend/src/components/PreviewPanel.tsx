import type { TemplateType, Template } from "@/types/template";
import type { ChannelState } from "@/types/useChannelSelection";
import ChannelPreview from "@/components/previews/ChannelPreview";

const CHANNELS: { type: TemplateType; label: string }[] = [
  { type: "sms", label: "SMS" },
  { type: "email", label: "Email" },
  { type: "push", label: "Push Notification" },
];

export default function PreviewPanel({
  channels,
  activePreview,
  onSelectPreview,
}: {
  channels: Record<TemplateType, ChannelState>;
  activePreview: TemplateType;
  onSelectPreview: (type: TemplateType) => void;
}) {

  const anyEnabled = channels.sms.enabled || channels.email.enabled || channels.push.enabled;
  const previewTemplate: Template | undefined = channels[activePreview].templates.find(
    (t) => t.templateId === channels[activePreview].templateId
  );

  return (
    <div className="bg-black/20 rounded-lg p-4 flex flex-col min-h-[320px]">
      {anyEnabled && (
        <div className="flex gap-1.5 mb-4 justify-center">
          {CHANNELS.filter((c) => channels[c.type].enabled).map((c) => (
            <button
              key={c.type}
              type="button"
              onClick={() => onSelectPreview(c.type)}
              className={`px-3 py-1 rounded-md text-xs font-medium transition ${
                activePreview === c.type ? "bg-[#2c4a63] text-white" : "text-gray-400 hover:text-gray-200"
              }`}
            >
              {c.label}
            </button>
          ))}
        </div>
      )}

      <div className="flex-1 flex items-center justify-center">
        {!anyEnabled ? (
          <p className="text-sm text-gray-500 text-center">Enable a channel to see a live preview here.</p>
        ) : !previewTemplate ? (
          <p className="text-sm text-gray-500 text-center">Select a template to preview it.</p>
        ) : (
          <ChannelPreview type={activePreview} name={previewTemplate.name} content={previewTemplate.content} />
        )}
      </div>
    </div>
  );
}
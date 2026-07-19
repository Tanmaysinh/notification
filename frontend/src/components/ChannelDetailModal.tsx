import Modal from "@/components/Modal";
import type { ChannelRow } from "@/types/report";

const CHANNEL_LABELS: Record<string, string> = { sms: "SMS", email: "Email", push: "Push Notification" };

export default function ChannelDetailModal({
  open,
  channel,
  onClose,
}: {
  open: boolean;
  channel: ChannelRow | null;
  onClose: () => void;
}) {
  if (!channel) return null;

  return (
    <Modal open={open} title={`${CHANNEL_LABELS[channel.channelType] ?? channel.channelType} Content`} onClose={onClose}>
      <div className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-3 text-sm text-gray-200 whitespace-pre-wrap break-words max-h-80 overflow-y-auto">
        {channel.content ?? <span className="text-gray-500 italic">Content unavailable</span>}
      </div>
    </Modal>
  );
}
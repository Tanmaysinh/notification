// import Modal from "@/components/Modal";
// import type { ChannelRow } from "@/types/report";

// const CHANNEL_LABELS: Record<string, string> = { sms: "SMS", email: "Email", push: "Push Notification" };

// export default function ChannelDetailModal({
//   open,
//   channel,
//   onClose,
//   onRetry,
//   retrying,
// }: {
//   open: boolean;
//   channel: ChannelRow | null;
//   onClose: () => void;
//   onRetry: () => void;
//   retrying: boolean;
// }) {
//   if (!channel) return null;

//   return (
//     <Modal open={open} title={`${CHANNEL_LABELS[channel.channelType] ?? channel.channelType} Details`} onClose={onClose}>
//       <div className="space-y-4">
//         <div>
//           <p className="text-xs text-gray-500 mb-1.5">Content Sent</p>
//           <div className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-3 text-sm text-gray-200 whitespace-pre-wrap break-words max-h-40 overflow-y-auto">
//             {channel.content ?? <span className="text-gray-500 italic">Content unavailable</span>}
//           </div>
//         </div>

//         <div>
//           <p className="text-xs text-gray-500 mb-1.5">Attempt History</p>
//           <div className="flex flex-wrap gap-1.5">
//             {channel.statusHistory.map((status, idx) => (
//               <span
//                 key={idx}
//                 className={`text-xs px-2 py-1 rounded-md ${
//                   status === "SENT"
//                     ? "bg-green-500/10 text-green-400"
//                     : status === "FAILED"
//                     ? "bg-red-500/10 text-red-400"
//                     : "bg-yellow-500/10 text-yellow-400"
//                 }`}
//               >
//                 Attempt {idx + 1}: {status}
//               </span>
//             ))}
//           </div>
//         </div>

//         <div className="flex items-center justify-between pt-2">
//           <p className="text-sm text-gray-400">
//             Retries used: <span className="text-white">{channel.retryCount}/3</span>
//           </p>

//           {channel.retryEligible && (
//             <button
//               onClick={onRetry}
//               disabled={retrying}
//               className="px-4 py-2 rounded-lg text-sm bg-[#2c4a63] text-white hover:opacity-90 disabled:opacity-50"
//             >
//               {retrying ? "Retrying…" : "Retry"}
//             </button>
//           )}
//         </div>
//       </div>
//     </Modal>
//   );
// }



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
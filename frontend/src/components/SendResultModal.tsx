import { useNavigate } from "react-router-dom";
import Modal from "@/components/Modal";

export default function SendResultModal({
  open,
  requestId,
  onClose,
}: {
  open: boolean;
  requestId: string | null;
  onClose: () => void;
}) {
  const navigate = useNavigate();

  if (!requestId) return null;

  return (
    <Modal open={open} title="Notification Sent" onClose={onClose}>
      <div className="text-center py-2">
        <div className="w-12 h-12 rounded-full bg-green-500/10 flex items-center justify-center mx-auto mb-4">
          <span className="text-green-400 text-2xl">✓</span>
        </div>
        <p className="text-sm text-gray-300 mb-1">Your notification has been queued for delivery.</p>
        <p className="text-xs text-gray-500 mb-4">Track its progress using the request ID below.</p>

        <div className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 mb-6">
          <p className="text-xs text-gray-500 mb-0.5">Request ID</p>
          <p className="text-sm text-white font-mono">{requestId}</p>
        </div>

        <div className="flex gap-3 justify-center">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg text-sm border border-white/10 text-gray-300 hover:bg-white/5"
          >
            Close
          </button>
          <button
            onClick={() => navigate(`/dashboard/report?requestId=${requestId}`)}
            className="px-4 py-2 rounded-lg text-sm bg-[#2c4a63] text-white hover:opacity-90"
          >
            View in Report
          </button>
        </div>
      </div>
    </Modal>
  );
}
export default function PushPreview({ title, content }: { title: string; content: string }) {
  return (
    <div className="bg-gradient-to-b from-gray-800 to-gray-900 rounded-[2rem] p-4 w-full max-w-[280px] mx-auto border border-white/10">
      <div className="text-center text-white text-xs mb-3 opacity-70">9:41</div>
      <div className="bg-white/95 backdrop-blur rounded-2xl p-3 shadow-lg">
        <div className="flex items-start gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-[#2c4a63] flex items-center justify-center shrink-0">
            <span className="text-white font-bold text-xs">V</span>
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-gray-900">VasyERP</p>
              <p className="text-[10px] text-gray-400">now</p>
            </div>
            <p className="text-sm font-medium text-gray-900 truncate">
              {title || <span className="text-gray-400 italic font-normal">Notification title…</span>}
            </p>
            <p className="text-xs text-gray-600 line-clamp-2">
              {content || <span className="text-gray-400 italic">Push message preview…</span>}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
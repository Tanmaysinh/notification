export default function SmsPreview({ content }: { content: string }) {
  return (
    <div className="bg-[#0f1420] rounded-2xl p-4 w-full max-w-[280px] mx-auto border border-white/10">
      <div className="text-center text-xs text-gray-500 mb-3">Messages</div>
      <div className="flex justify-start">
        <div className="bg-[#2c2c2e] text-white text-sm rounded-2xl rounded-bl-md px-4 py-2.5 max-w-[85%] whitespace-pre-wrap break-words">
          {content || <span className="text-gray-500 italic">SMS content preview…</span>}
        </div>
      </div>
    </div>
  );
}
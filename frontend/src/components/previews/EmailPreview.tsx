export default function EmailPreview({ subject, content }: { subject: string; content: string }) {
  return (
    <div className="bg-white rounded-xl overflow-hidden w-full max-w-md mx-auto shadow-lg">
      <div className="bg-gray-100 px-4 py-2.5 border-b flex items-center gap-2">
        <div className="flex gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-red-400" />
          <span className="w-2.5 h-2.5 rounded-full bg-yellow-400" />
          <span className="w-2.5 h-2.5 rounded-full bg-green-400" />
        </div>
      </div>
      <div className="px-5 py-4 border-b">
        <p className="text-xs text-gray-500 mb-1">VasyERP &lt;notifications@vasyerp.com&gt;</p>
        <p className="text-base font-semibold text-gray-900">
          {subject || <span className="text-gray-400 italic font-normal">Subject line…</span>}
        </p>
      </div>
      <div className="px-5 py-5 text-sm text-gray-700 whitespace-pre-wrap break-words min-h-[100px]">
        {content || <span className="text-gray-400 italic">Email body preview…</span>}
      </div>
    </div>
  );
}
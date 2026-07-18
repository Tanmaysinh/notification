import type { TemplateType } from "@/types/template";
import SmsPreview from "./SmsPreview";
import EmailPreview from "./EmailPreview";
import PushPreview from "./PushPreview";

export default function ChannelPreview({
  type,
  name,
  content,
}: {
  type: TemplateType;
  name: string;
  content: string;
}) {
  if (type === "sms") return <SmsPreview content={content} />;
  if (type === "email") return <EmailPreview subject={name} content={content} />;
  return <PushPreview title={name} content={content} />;
}
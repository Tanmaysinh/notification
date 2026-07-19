export type TemplateType = "sms" | "email" | "push";

export interface Template {
  templateId: string;
  name: string;
  content: string;
}

export type TemplateFormValues = Omit<Template, "templateId">;
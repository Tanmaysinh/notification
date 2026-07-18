export interface Campaign {
  campaignId: string;
  name: string;
  smsTemplateId: string | null;
  smsTemplateName: string | null;
  emailTemplateId: string | null;
  emailTemplateName: string | null;
  pushTemplateId: string | null;
  pushTemplateName: string | null;
}

export interface CampaignFormValues {
  name: string;
  smsTemplateId: string | null;
  emailTemplateId: string | null;
  pushTemplateId: string | null;
}
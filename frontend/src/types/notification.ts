export interface SendNotificationRequest {
  campaignId: string | null;
  smsTemplateId: string | null;
  emailTemplateId: string | null;
  pushTemplateId: string | null;
  contactIds: string[];
}

export interface SendNotificationResponse {
  requestId: string;
}
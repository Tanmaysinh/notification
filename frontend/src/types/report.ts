
export interface ReportFilters {
  dateFrom?: string | null;
  dateTo?: string | null;
  requestId?: string;
  notificationType?: string;
  requestStatus?: string;
  notificationStatus?: string;
  contactSearch?: string;
  campaignId?: string;
  page: number;
  size: number;
}

export interface ChannelRow {
  channelType: string;
  content: string | null;
  statusHistory: string[];
  latestStatus: string;
  retryCount: number;
  retryEligible: boolean;
  userData: string;

}

export interface ReportRow {
  requestId: string;
  campaignId: string | null;
  campaignName: string | null;
  scheduleTime: string | null;
  createdAt: string;
  requestStatus: string;
  contactId: string;
  contactName: string;
  contactEmail: string | null;
  contactPhone: string | null;
  channels: ChannelRow[];
}
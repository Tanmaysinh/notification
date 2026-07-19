export interface DailyCount {
  date: string;
  count: number;
}

export interface DashboardSummary {
  totalRequests: number;
  totalRecipients: number;
  requestsByStatus: Record<string, number>;
  statusByChannel: Record<string, Record<string, number>>;
  retriesByChannel: Record<string, number>;
  requestsOverTime: DailyCount[];
}
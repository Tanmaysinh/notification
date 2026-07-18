export interface NavItem {
  label: string;
  path?: string;
  icon: string; // simple label for now, swap for real icons later
  children?: { label: string; path: string }[];
}

export const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", path: "/dashboard", icon: "📊" },
  { label: "Contacts", path: "/dashboard/contacts", icon: "👥" },
  {
    label: "Template Management",
    icon: "📄",
    children: [
      { label: "SMS", path: "/dashboard/templates/sms" },
      { label: "Email", path: "/dashboard/templates/email" },
      { label: "Push", path: "/dashboard/templates/push" },
    ],
  },
  { label: "Campaign", path: "/dashboard/campaign", icon: "📢" },
  { label: "Send Notification", path: "/dashboard/send-notification", icon: "🔔" },
  { label: "Report", path: "/dashboard/report", icon: "📈" },
];
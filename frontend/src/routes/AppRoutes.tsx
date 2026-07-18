import { Routes, Route, Navigate } from "react-router-dom";
import AuthPage from "@/auth/AuthPage";
import DashboardLayout from "@/layout/DashboardLayout";
import DashboardPage from "@/pages/DashboardPage";
import ContactsPage from "@/pages/ContactsPage";
import SmsTemplatePage from "@/pages/templates/SmsTemplatePage";
import EmailTemplatePage from "@/pages/templates/EmailTemplatePage";
import PushTemplatePage from "@/pages/templates/PushTemplatePage";
import CampaignPage from "@/pages/CampaignPage";
import SendNotificationPage from "@/pages/SendNotificationPage";
import ReportPage from "@/pages/ReportPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<AuthPage />} />

      <Route path="/dashboard" element={<DashboardLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="contacts" element={<ContactsPage />} />
        <Route path="templates/sms" element={<SmsTemplatePage />} />
        <Route path="templates/email" element={<EmailTemplatePage />} />
        <Route path="templates/push" element={<PushTemplatePage />} />
        <Route path="campaign" element={<CampaignPage />} />
        <Route path="send-notification" element={<SendNotificationPage />} />
        <Route path="report" element={<ReportPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
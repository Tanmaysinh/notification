// frontend/src/App.tsx
import { SecureSessionProvider } from "@/crypto/SecureSessionContext";
import AppRoutes from "@/routes/AppRoutes";

export default function App() {
  return (
    <SecureSessionProvider>
      <AppRoutes />
    </SecureSessionProvider>
  );
}
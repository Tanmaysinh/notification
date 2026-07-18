import { useState } from "react";
import { useSecureSession } from "@/crypto/SecureSessionContext";

type Mode = "login" | "signup";

interface AuthResponse {
  token: string;
  user: { id: string; email: string; name: string };
}

export default function AuthPage() {
  const { ready, error: sessionError, secureFetch } = useSecureSession();
  const [mode, setMode] = useState<Mode>("login");
  const [form, setForm] = useState({ name: "", email: "", password: "", confirmPassword: "" });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [signupSuccess, setSignupSuccess] = useState(false);

  function update(field: keyof typeof form, value: string) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  function switchMode(nextMode: Mode) {
    setMode(nextMode);
    setFormError(null);
    setSignupSuccess(false);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError(null);

    if (mode === "signup" && form.password !== form.confirmPassword) {
      setFormError("Passwords do not match.");
      return;
    }

    setSubmitting(true);
    try {
      if (mode === "signup") {
        await secureFetch<AuthResponse>("/api/auth/signup", {
          name: form.name,
          email: form.email,
          password: form.password,
        });

        // Don't log the user in automatically — send them to the login tab
        // with a success notice, keeping the email prefilled for convenience.
        setMode("login");
        setSignupSuccess(true);
        setForm((f) => ({ ...f, password: "", confirmPassword: "" }));
      } else {
        const data = await secureFetch<AuthResponse>("/api/auth/login", {
          email: form.email,
          password: form.password,
        });

        // Store however you've decided to handle the token — httpOnly cookie
        // set by the backend is preferable; this is a placeholder if you're
        // temporarily using localStorage during development.
        localStorage.setItem("token", data.token);
        localStorage.setItem("user", JSON.stringify(data.user));

        window.location.href = "/dashboard";
      }
    } catch (err: any) {
      setFormError(err.message ?? "Something went wrong. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  if (sessionError) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#0f1420]">
        <p className="text-red-400 text-sm">
          Couldn't establish a secure connection. Please refresh and try again.
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0f1420] px-4">
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -left-40 w-96 h-96 bg-[#2c4a63] opacity-20 rounded-full blur-3xl" />
        <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-[#2c4a63] opacity-10 rounded-full blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-[#2c4a63] mb-4">
            <span className="text-white font-bold text-lg">V</span>
          </div>
          <h1 className="text-2xl font-semibold text-white">
            {mode === "login" ? "Welcome back" : "Create your account"}
          </h1>
          <p className="text-sm text-gray-400 mt-1">
            {mode === "login" ? "Log in to your VasyERP dashboard" : "Get started with VasyERP"}
          </p>
        </div>

        <div className="bg-[#171d2b] border border-white/10 rounded-2xl shadow-xl p-8">
          <div className="flex mb-6 border border-white/10 rounded-lg p-1 bg-[#0f1420]">
            <button
              type="button"
              onClick={() => switchMode("login")}
              className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
                mode === "login" ? "bg-[#2c4a63] text-white" : "text-gray-400 hover:text-gray-200"
              }`}
            >
              Log in
            </button>
            <button
              type="button"
              onClick={() => switchMode("signup")}
              className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
                mode === "signup" ? "bg-[#2c4a63] text-white" : "text-gray-400 hover:text-gray-200"
              }`}
            >
              Sign up
            </button>
          </div>

          {signupSuccess && mode === "login" && (
            <div className="bg-green-500/10 border border-green-500/20 rounded-lg px-4 py-2.5 mb-4">
              <p className="text-sm text-green-400">
                Account created. Log in with your new credentials.
              </p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {mode === "signup" && (
              <div>
                <label className="text-sm text-gray-400 block mb-1.5">Full name</label>
                <input
                  type="text"
                  required
                  value={form.name}
                  onChange={(e) => update("name", e.target.value)}
                  className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white placeholder-gray-500 focus:border-[#2c4a63] focus:ring-1 focus:ring-[#2c4a63] transition text-sm"
                  placeholder="Jane Doe"
                />
              </div>
            )}

            <div>
              <label className="text-sm text-gray-400 block mb-1.5">Email</label>
              <input
                type="email"
                required
                value={form.email}
                onChange={(e) => update("email", e.target.value)}
                className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white placeholder-gray-500 focus:border-[#2c4a63] focus:ring-1 focus:ring-[#2c4a63] transition text-sm"
                placeholder="you@company.com"
                autoComplete="username"
              />
            </div>

            <div>
              <label className="text-sm text-gray-400 block mb-1.5">Password</label>
              <input
                type="password"
                required
                minLength={8}
                value={form.password}
                onChange={(e) => update("password", e.target.value)}
                className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white placeholder-gray-500 focus:border-[#2c4a63] focus:ring-1 focus:ring-[#2c4a63] transition text-sm"
                placeholder="••••••••"
                autoComplete={mode === "login" ? "current-password" : "new-password"}
              />
            </div>

            {mode === "signup" && (
              <div>
                <label className="text-sm text-gray-400 block mb-1.5">Confirm password</label>
                <input
                  type="password"
                  required
                  value={form.confirmPassword}
                  onChange={(e) => update("confirmPassword", e.target.value)}
                  className="bg-[#0f1420] border border-white/10 rounded-lg px-4 py-2.5 w-full outline-none text-white placeholder-gray-500 focus:border-[#2c4a63] focus:ring-1 focus:ring-[#2c4a63] transition text-sm"
                  placeholder="••••••••"
                  autoComplete="new-password"
                />
              </div>
            )}

            {formError && (
              <div className="bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-2.5">
                <p className="text-sm text-red-400">{formError}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={!ready || submitting}
              className="w-full bg-[#2c4a63] text-white py-2.5 rounded-lg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition mt-2"
            >
              {!ready
                ? "Securing connection…"
                : submitting
                ? mode === "login"
                  ? "Logging in…"
                  : "Creating account…"
                : mode === "login"
                ? "Log in"
                : "Create account"}
            </button>
          </form>
        </div>

        <p className="text-center text-xs text-gray-500 mt-6">
          Secured with end-to-end encrypted authentication
        </p>
      </div>
    </div>
  );
}
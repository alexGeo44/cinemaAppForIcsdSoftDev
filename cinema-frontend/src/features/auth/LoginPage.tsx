import { useState } from "react";
import { authStore } from "../../auth/auth.store";
import { Link, useLocation, useNavigate } from "react-router-dom";

export default function LoginPage() {
  const login = authStore((s) => s.login);
  const nav = useNavigate();
  const location = useLocation();

  const [username, setU] = useState("");
  const [password, setP] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const extractServerMessage = (e: any) => {
    const status = e?.response?.status;
    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.message ||
      "Λάθος στοιχεία ή πρόβλημα στο login.";

    if (status === 400) return `400 Bad Request: ${msg}`;
    if (status === 401) return `401 Unauthorized: ${msg}`;
    if (status === 403) return `403 Forbidden: ${msg}`;
    return msg;
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loading) return;

    setError(null);

    const u = username.trim();
    const p = password;

    if (!u || !p) {
      setError("Συμπλήρωσε username και password.");
      return;
    }

    setLoading(true);
    try {
      await login(u, p);

      // ✅ αν ήρθες από AuthGuard, γύρνα εκεί
      const from = (location.state as any)?.from?.pathname as string | undefined;
      nav(from || "/", { replace: true });
    } catch (err: any) {
      setError(extractServerMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center px-4">
      <div className="max-w-md w-full">
        <div className="mb-6 text-center">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-sky-500/15 border border-sky-500/40 text-sky-400 text-xl font-bold shadow-lg shadow-sky-900/40">
            CM
          </div>
          <h1 className="mt-4 text-2xl font-semibold text-slate-50">
            Sign in to Cinema Manager
          </h1>
          <p className="mt-1 text-sm text-slate-400">
            Βάλε τα στοιχεία σου για να συνεχίσεις.
          </p>
        </div>

        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl shadow-2xl shadow-slate-950/70 px-6 py-6">
          {error && (
            <div className="mb-4 text-xs rounded-md bg-rose-500/10 border border-rose-500/60 text-rose-100 px-3 py-2">
              {error}
            </div>
          )}

          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-200 mb-1">
                Username
              </label>
              <input
                name="username"
                autoComplete="username"
                value={username}
                onChange={(e) => setU(e.target.value)}
                disabled={loading}
                className="w-full rounded-md border border-slate-700 bg-slate-950/80 px-3 py-2 text-sm text-slate-50 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-sky-500 disabled:opacity-60"
                placeholder="π.χ. admin01"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-200 mb-1">
                Password
              </label>
              <input
                name="password"
                autoComplete="current-password"
                type="password"
                value={password}
                onChange={(e) => setP(e.target.value)}
                disabled={loading}
                className="w-full rounded-md border border-slate-700 bg-slate-950/80 px-3 py-2 text-sm text-slate-50 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-sky-500 disabled:opacity-60"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="mt-2 w-full rounded-lg bg-sky-500 hover:bg-sky-400 text-slate-950 font-semibold text-sm py-2.5 transition-colors shadow-lg shadow-sky-900/50 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? "Logging in..." : "Login"}
            </button>
          </form>

          <div className="mt-4 text-xs text-slate-500 flex justify-between">
            <span>Demo admin: admin01 / Admin123!</span>
            <Link
              to="/register"
              className="text-sky-400 hover:text-sky-300"
              onClick={(e) => {
                if (loading) e.preventDefault();
              }}
            >
              Create account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

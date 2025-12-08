import { useState } from "react";
import { authStore } from "../../auth/auth.store";
import { useNavigate, Link } from "react-router-dom";

export default function LoginPage() {
  const login = authStore((s) => s.login);
  const nav = useNavigate();

  const [username, setU] = useState("");
  const [password, setP] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username, password);
      nav("/");
    } catch (err: any) {
      setError("Λάθος στοιχεία ή πρόβλημα στο login.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100">
      <div className="w-full max-w-md bg-white rounded-xl shadow-lg p-8">
        <h1 className="text-2xl font-bold text-slate-900 mb-2">
          Sign in to Cinema Manager
        </h1>
        <p className="text-sm text-slate-500 mb-6">
          Βάλε τα στοιχεία σου για να συνεχίσεις.
        </p>

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Username
            </label>
            <input
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-slate-900"
              value={username}
              onChange={(e) => setU(e.target.value)}
              autoComplete="username"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Password
            </label>
            <input
              type="password"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-slate-900"
              value={password}
              onChange={(e) => setP(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-md px-3 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-2 inline-flex items-center justify-center rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-60 disabled:cursor-not-allowed transition"
          >
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <div className="mt-6 text-xs text-slate-500 flex justify-between">
          <span>Demo project – cinema DDD</span>
          <Link to="/register" className="underline">
            Create account
          </Link>
        </div>
      </div>
    </div>
  );
}

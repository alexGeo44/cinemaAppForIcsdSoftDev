import { authStore } from "../../auth/auth.store";

export default function Navbar() {
  const user = authStore((s) => s.user);
  const logout = authStore((s) => s.logout);

  return (
    <header className="h-14 flex items-center justify-between px-6 border-b bg-white shadow-sm">
      <div className="font-semibold text-slate-800 tracking-tight">
        Cinema Manager
      </div>
      <div className="flex items-center gap-3">
        {user && (
          <>
            <div className="text-sm text-slate-600 text-right">
              <div className="font-medium">{user.fullName}</div>
              <div className="text-xs uppercase tracking-wide">
                {user.role}
              </div>
            </div>
            <button
              onClick={logout}
              className="text-sm px-3 py-1 rounded-md border border-slate-300 hover:bg-slate-100 transition"
            >
              Logout
            </button>
          </>
        )}
      </div>
    </header>
  );
}

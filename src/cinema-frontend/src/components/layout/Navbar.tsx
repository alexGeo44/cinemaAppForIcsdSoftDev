import { authStore } from "../../auth/auth.store";

export default function Navbar() {
  const user = authStore((s) => s.user);
  const logout = authStore((s) => s.logout);

  return (
    <header className="h-14 flex items-center justify-between px-6 border-b border-slate-800 bg-slate-950/95 backdrop-blur shadow-sm shadow-slate-950/60">
      <div className="flex items-center gap-2">
        <div className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-sky-500/15 border border-sky-500/40 text-sky-400 text-sm font-bold">
          CM
        </div>
        <div className="flex flex-col leading-tight">
          <span className="font-semibold tracking-tight text-slate-50">
            Cinema Manager
          </span>
          <span className="text-[11px] text-slate-400">
            Festival programs & screenings
          </span>
        </div>
      </div>

      {user && (
        <div className="flex items-center gap-4">
          <div className="text-right text-xs">
            <div className="text-slate-50 font-medium">
              {user.fullName}
            </div>
            <div className="text-slate-400">
              {user.role}
            </div>
          </div>
          <button
            onClick={logout}
            className="text-xs px-3 py-1.5 rounded-md border border-slate-700 bg-slate-900 hover:bg-slate-800 text-slate-100 transition"
          >
            Logout
          </button>
        </div>
      )}
    </header>
  );
}

import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useMemo } from "react";
import { authStore } from "../../auth/auth.store";
import { canCreateProgram, canViewMyScreenings, isAdmin, isCinemaUser } from "../../auth/permissions";

function linkClass({ isActive }: { isActive: boolean }) {
  return [
    "text-xs px-3 py-1.5 rounded-md border transition",
    isActive
      ? "border-sky-500/60 bg-sky-500/10 text-sky-200"
      : "border-slate-700 bg-slate-900 hover:bg-slate-800 text-slate-100",
  ].join(" ");
}

export default function Navbar() {
  const user = authStore((s) => s.user);
  const token = authStore((s) => s.token);
  const bootstrapped = authStore((s) => s.bootstrapped);
  const logout = authStore((s) => s.logout);

  const nav = useNavigate();
  const location = useLocation();

  const perms = useMemo(() => {
    const role = user?.role;
    return {
      isAdmin: isAdmin(role),
      isCinemaUser: isCinemaUser(role), // USER only
      canCreateProgram: canCreateProgram(role),
      canMyScreenings: canViewMyScreenings(role),
    };
  }, [user?.role]);

  const onLogout = () => {
    logout();
    nav("/login", { replace: true, state: { from: location.pathname } });
  };

  // ⏳ μέχρι να γίνει bootstrap (token υπάρχει αλλά δεν ξέρουμε user ακόμα)
  if (token && !bootstrapped) {
    return (
      <header className="h-14 flex items-center justify-between px-6 border-b border-slate-800 bg-slate-950/95 backdrop-blur shadow-sm shadow-slate-950/60">
        <div className="flex items-center gap-3">
          <div className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-sky-500/15 border border-sky-500/40 text-sky-400 text-sm font-bold">
            CM
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-semibold tracking-tight text-slate-50">Cinema Manager</span>
            <span className="text-[11px] text-slate-400">Loading user…</span>
          </div>
        </div>
      </header>
    );
  }

  return (
    <header className="h-14 flex items-center justify-between px-6 border-b border-slate-800 bg-slate-950/95 backdrop-blur shadow-sm shadow-slate-950/60">
      <div className="flex items-center gap-3">
        <div className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-sky-500/15 border border-sky-500/40 text-sky-400 text-sm font-bold">
          CM
        </div>

        <div className="flex flex-col leading-tight">
          <span className="font-semibold tracking-tight text-slate-50">Cinema Manager</span>
          <span className="text-[11px] text-slate-400">Festival programs & screenings</span>
        </div>

        {user && (
          <nav className="ml-6 flex items-center gap-2">
            {/* όλοι οι authenticated */}
            <NavLink to="/programs" className={linkClass}>
              Programs
            </NavLink>

            {/* USER domain */}
            {perms.isCinemaUser && (
              <>
                {perms.canMyScreenings && (
                  <NavLink to="/my-screenings" className={linkClass}>
                    My Screenings
                  </NavLink>
                )}

                {/* program-scoped: σελίδες/enpoints θα κόβουν μέσα */}
                <NavLink to="/staff/review" className={linkClass}>
                  Staff Review
                </NavLink>

                <NavLink to="/programmer/screenings" className={linkClass}>
                  Programmer
                </NavLink>

                {/* optional shortcut */}
                {perms.canCreateProgram && (
                  <NavLink to="/programs/new" className={linkClass}>
                    New program
                  </NavLink>
                )}
              </>
            )}

            {/* ADMIN */}
            {perms.isAdmin && (
              <>
                <NavLink to="/admin/users" className={linkClass}>
                  Users
                </NavLink>
                <NavLink to="/admin/audit-log" className={linkClass}>
                  Audit
                </NavLink>
              </>
            )}

            <NavLink to="/account" className={linkClass}>
              Account
            </NavLink>
          </nav>
        )}
      </div>

      {user ? (
        <div className="flex items-center gap-4">
          <div className="text-right text-xs">
            <div className="text-slate-50 font-medium">{user.fullName}</div>
            <div className="text-slate-400">{String(user.role)}</div>
          </div>

          <button
            onClick={onLogout}
            className="text-xs px-3 py-1.5 rounded-md border border-slate-700 bg-slate-900 hover:bg-slate-800 text-slate-100 transition"
          >
            Logout
          </button>
        </div>
      ) : (
        <div className="flex items-center gap-2 text-xs">
          <NavLink to="/login" className={linkClass}>
            Login
          </NavLink>
          <NavLink
            to="/register"
            className="text-xs px-3 py-1.5 rounded-md border border-slate-700 hover:bg-slate-900 text-slate-200 transition"
          >
            Register
          </NavLink>
        </div>
      )}
    </header>
  );
}

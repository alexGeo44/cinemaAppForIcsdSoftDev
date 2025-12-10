import { NavLink } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";

function linkClass({ isActive }: { isActive: boolean }) {
  return [
    "block rounded-md px-3 py-2 text-sm transition",
    isActive
      ? "bg-sky-500 text-slate-950 font-semibold shadow-sm"
      : "text-slate-200 hover:bg-slate-800/70 hover:text-white",
  ].join(" ");
}

export default function Sidebar() {
  const user = authStore((s) => s.user);
  const role = user?.role;

  const isLoggedIn = !!user;
  const isAdmin = role === BaseRole.ADMIN;
  const isProgrammer = role === BaseRole.PROGRAMMER;
  const isStaff = role === BaseRole.STAFF;
  const isSubmitter = role === BaseRole.SUBMITTER || role === BaseRole.USER;

  return (
    <aside className="w-64 border-r border-slate-800 bg-slate-950/90">
      <nav className="px-3 py-4 space-y-6 text-slate-300 text-sm">
        {/* GENERAL */}
        <div>
          <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
            General
          </div>
          <NavLink to="/programs" className={linkClass}>
            Programs
          </NavLink>
        </div>

        {/* USER VIEWS (My screenings + Account) */}
        {isLoggedIn && (
          <div>
            <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
              My area
            </div>
            {isSubmitter && (
              <NavLink to="/my-screenings" className={linkClass}>
                My screenings
              </NavLink>
            )}
            <NavLink to="/account" className={linkClass}>
              Account settings
            </NavLink>
          </div>
        )}

        {/* PROGRAMMER */}
        {(isProgrammer || isAdmin) && (
          <div>
            <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
              Programmer
            </div>
            <NavLink to="/programmer/screenings" className={linkClass}>
              Program screenings
            </NavLink>
          </div>
        )}

        {/* STAFF */}
        {(isStaff || isAdmin) && (
          <div>
            <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
              Staff
            </div>
            <NavLink to="/staff/review" className={linkClass}>
              Review screenings
            </NavLink>
          </div>
        )}

        {/* ADMIN */}
        {isAdmin && (
          <div>
            <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
              Admin
            </div>

            <NavLink to="/admin/users" className={linkClass}>
              User management
            </NavLink>

            <NavLink to="/admin/audit-log" className={linkClass}>
              Audit log
            </NavLink>
          </div>
        )}
      </nav>
    </aside>
  );
}

import { NavLink } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { canViewMyScreenings, isAdmin, isCinemaUser } from "../../auth/permissions";

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

  // αν είσαι visitor, μην δείχνεις sidebar (ή άστο να δείχνει μόνο Programs, αν θες)
  if (!role) return null;

  const cinemaUser = isCinemaUser(role); // USER only

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

        {/* MY AREA (USER) */}
        {cinemaUser && (
          <div>
            <div className="px-3 mb-1 text-[11px] font-semibold text-slate-500 uppercase tracking-wide">
              My area
            </div>

            {canViewMyScreenings(role) && (
              <NavLink to="/my-screenings" className={linkClass}>
                My screenings
              </NavLink>
            )}

            {/* Program-scoped pages: δείχνουμε link σε όλους τους USER.
               Η κάθε σελίδα θα κόψει μέσα (assigned staff / programmer-of-program). */}
            <NavLink to="/programmer/screenings" className={linkClass}>
              Program screenings
            </NavLink>

            <NavLink to="/staff/review" className={linkClass}>
              Review screenings
            </NavLink>

            <NavLink to="/account" className={linkClass}>
              Account settings
            </NavLink>
          </div>
        )}

        {/* ADMIN */}
        {isAdmin(role) && (
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

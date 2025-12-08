import { NavLink } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";

function linkClass({ isActive }: { isActive: boolean }) {
  return [
    "block px-3 py-2 rounded-md text-sm",
    isActive
      ? "bg-slate-900 text-white font-medium"
      : "text-slate-700 hover:bg-slate-100",
  ].join(" ");
}

export default function Sidebar() {
  const user = authStore((s) => s.user);

  if (!user) return null;

  const isProgrammer =
    user.role === BaseRole.PROGRAMMER || user.role === BaseRole.ADMIN;
  const isStaff = user.role === BaseRole.STAFF || user.role === BaseRole.ADMIN;
  const isAdmin = user.role === BaseRole.ADMIN;

  return (
    <aside className="w-60 border-r bg-white px-3 py-4">
      <nav className="space-y-6 text-sm">
        <div>
          <div className="px-3 mb-1 text-xs font-semibold text-slate-400">
            GENERAL
          </div>
          <NavLink to="/" className={linkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/programs" className={linkClass}>
            Programs
          </NavLink>
          <NavLink to="/my-screenings" className={linkClass}>
            My screenings
          </NavLink>
          <NavLink to="/account" className={linkClass}>
            Account
          </NavLink>
        </div>

        {isProgrammer && (
          <div>
            <div className="px-3 mb-1 text-xs font-semibold text-slate-400">
              PROGRAMMER
            </div>
            <NavLink
              to="/programmer/screenings"
              className={linkClass}
            >
              Program screenings
            </NavLink>
          </div>
        )}

        {isStaff && (
          <div>
            <div className="px-3 mb-1 text-xs font-semibold text-slate-400">
              STAFF
            </div>
            <NavLink to="/staff/review" className={linkClass}>
              Review queue
            </NavLink>
          </div>
        )}

        {isAdmin && (
          <div>
            <div className="px-3 mb-1 text-xs font-semibold text-slate-400">
              ADMIN
            </div>
            <NavLink to="/admin/users" className={linkClass}>
              User management
            </NavLink>
          </div>
        )}
      </nav>
    </aside>
  );
}

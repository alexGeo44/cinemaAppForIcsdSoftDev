// src/app/guards/RoleGuard.tsx
import { Navigate, useLocation } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import type { UserResponse } from "../../domain/auth/auth.types";
import { BaseRole } from "../../domain/auth/auth.types";
import { normalizeRole } from "../../auth/role";

export function RoleGuard({
  allow,
  allowIf,
  children,
}: {
  allow?: BaseRole[];
  // ✅ backward compatible: allowIf(role) still works, but can also use allowIf(role, user)
  allowIf?: (role?: BaseRole, user?: UserResponse | null) => boolean;
  children: JSX.Element;
}) {
  const user = authStore((s) => s.user);
  const bootstrapped = authStore((s) => s.bootstrapped);
  const location = useLocation();

  // ✅ don't flash blank screen
  if (!bootstrapped) {
    return (
      <div className="h-screen flex items-center justify-center text-sm text-slate-400">
        Checking permissions…
      </div>
    );
  }

  // If not logged in, send to login (robust even if AuthGuard not used)
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  const role = normalizeRole(user.role);

  const ok =
    typeof allowIf === "function"
      ? allowIf(role, user)
      : Array.isArray(allow)
      ? !!role && allow.includes(role)
      : true;

  if (!ok) return <Navigate to="/forbidden" replace />;

  return children;
}

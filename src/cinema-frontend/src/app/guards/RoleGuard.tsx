import { Navigate } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { BaseRole } from "../../domain/auth/auth.types";

export function RoleGuard({
  allow,
  children,
}: {
  allow: BaseRole[];
  children: JSX.Element;
}) {
  const user = authStore((s) => s.user);
  if (!user || !allow.includes(user.role)) {
    return <Navigate to="/forbidden" />;
  }
  return children;
}

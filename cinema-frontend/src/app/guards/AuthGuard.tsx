// src/app/guards/AuthGuard.tsx
import { useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { authStore } from "../../auth/auth.store";

type Props = { children: JSX.Element };

export function AuthGuard({ children }: Props) {
  const user = authStore((s) => s.user);
  const token = authStore((s) => s.token);
  const bootstrapped = authStore((s) => s.bootstrapped);
  const loadMe = authStore((s) => s.loadMe);

  const location = useLocation();

  useEffect(() => {
    // ✅ always try to bootstrap once (loadMe handles "no token" too)
    if (!bootstrapped) {
      loadMe();
    }
  }, [bootstrapped, loadMe]);

  // ✅ Not bootstrapped yet -> show loader (prevents flicker)
  if (!bootstrapped) {
    return (
      <div className="h-screen flex items-center justify-center text-sm text-slate-400">
        Checking authentication…
      </div>
    );
  }

  // ✅ After bootstrap: if no token or no user => go login
  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

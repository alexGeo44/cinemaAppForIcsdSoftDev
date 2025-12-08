import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { authStore } from "../../auth/auth.store";

type Props = {
  children: JSX.Element;
};

export function AuthGuard({ children }: Props) {
  const user = authStore((s) => s.user);
  const location = useLocation();

  // Αν δεν υπάρχει user -> redirect στο /login
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // Αλλιώς δείξε κανονικά τη σελίδα
  return children;
}

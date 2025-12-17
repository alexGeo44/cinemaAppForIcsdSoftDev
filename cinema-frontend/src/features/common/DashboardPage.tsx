// src/features/common/DashboardPage.tsx
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { authStore } from "../../auth/auth.store";
import { normalizeRole } from "../../auth/role";
import { BaseRole } from "../../domain/auth/auth.types";

export default function DashboardPage() {
  const nav = useNavigate();

  const token = authStore((s) => s.token);
  const user = authStore((s) => s.user);
  const bootstrapped = authStore((s) => s.bootstrapped);
  const loadMe = authStore((s) => s.loadMe);

  // Αν έχει token αλλά δεν έχει φορτώσει user (refresh), κάνε bootstrap
  useEffect(() => {
    if (token && !bootstrapped) loadMe();
  }, [token, bootstrapped, loadMe]);

  useEffect(() => {
    // Visitor
    if (!token) {
      nav("/programs", { replace: true });
      return;
    }

    // Logged-in αλλά ακόμα φορτώνει
    if (!bootstrapped || !user) return;

    const role = normalizeRole(user.role);

    // Redirect ανά ρόλο σύμφωνα με spec
    if (role === BaseRole.STAFF) {
      nav("/staff/review", { replace: true });
    } else if (role === BaseRole.SUBMITTER) {
      nav("/my-screenings", { replace: true });
    } else {
      // USER / PROGRAMMER / ADMIN -> Programs ως default landing
      nav("/programs", { replace: true });
    }
  }, [token, bootstrapped, user, nav]);

  return (
    <div className="h-[60vh] flex items-center justify-center text-sm text-slate-400">
      Loading…
    </div>
  );
}

// src/auth/can.ts
import { authStore } from "./auth.store"; // ⚠️ αν το δικό σου export λέγεται useAuthStore, άλλαξέ το
import { BaseRole } from "@/domain/auth/auth.types";

type Role = BaseRole | string | null | undefined;

const normalizeRole = (role: Role): BaseRole | undefined => {
if (!role) return undefined;

  let r = String(role).trim().toUpperCase();
  while (r.startsWith("ROLE_")) r = r.slice(5);

  return (Object.values(BaseRole) as string[]).includes(r)
    ? (r as BaseRole)
    : undefined;
};

// helper που ελέγχει roles χωρίς να βασίζεται σε auth.is(...)
function hasAnyRole(roles: BaseRole | BaseRole[]) {
  const { user } = authStore.getState(); // ✅ δεν χρειάζεται hook εδώ
  const r = normalizeRole(user?.role);
  if (!r) return false;

  return Array.isArray(roles) ? roles.includes(r) : r === roles;
}

// --------------------
// CANs
// --------------------
export function canManageUsers() {
  return hasAnyRole(BaseRole.ADMIN);
}

// ✅ σύμφωνα με το spec σου: ΜΟΝΟ PROGRAMMER manage programs (όχι ADMIN)
export function canManagePrograms() {
  return hasAnyRole(BaseRole.PROGRAMMER);
}

//  STAFF μόνο (όχι ADMIN) αν αυτό θες στο UI
export function canReviewScreenings() {
  return hasAnyRole(BaseRole.STAFF);
}

//submit screenings: USER/SUBMITTER/STAFF/PROGRAMMER (όπως είχες στα permissions)
export function canSubmitScreenings() {
  return hasAnyRole([
    BaseRole.USER,
    BaseRole.SUBMITTER,
    BaseRole.STAFF,
    BaseRole.PROGRAMMER,
  ]);
}

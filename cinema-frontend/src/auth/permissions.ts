import { BaseRole } from "../domain/auth/auth.types";

type Role = BaseRole | string | undefined;

const normalizeRole = (role: Role): BaseRole | undefined => {
if (!role) return undefined;

  const r = String(role).trim().toUpperCase().replace(/^ROLE_/, "");
  return (Object.values(BaseRole) as string[]).includes(r)
    ? (r as BaseRole)
    : undefined;
};

const R = (role?: Role) => normalizeRole(role);

// --------------------
// BASIC
// --------------------
export const isAdmin = (role?: Role) => R(role) === BaseRole.ADMIN;

// --------------------
// PROGRAMS (SPEC)
// USER/SUBMITTER/STAFF/PROGRAMMER μπορούν create
// μόνο PROGRAMMER manage
// --------------------
export const canCreateProgram = (role?: Role) => {
  const r = R(role);
  return (
    r === BaseRole.USER ||
    r === BaseRole.SUBMITTER ||
    r === BaseRole.STAFF ||
    r === BaseRole.PROGRAMMER
  );
};

export const canManageProgram = (role?: Role) => R(role) === BaseRole.PROGRAMMER;

// --------------------
// SCREENINGS (SPEC)
// USER/SUBMITTER μπορούν create (και οι άλλοι έχουν “All USER functions”)
// όμως: PROGRAMMER ΔΕΝ πρέπει να submit σε own program -> το κόβει backend
// --------------------
export const canCreateScreening = (role?: Role) => {
  const r = R(role);
  return (
    r === BaseRole.USER ||
    r === BaseRole.SUBMITTER ||
    r === BaseRole.STAFF ||
    r === BaseRole.PROGRAMMER
  );
};

// “My screenings” (full details) μόνο SUBMITTER στο spec σου
export const canViewMyScreenings = (role?: Role) => R(role) === BaseRole.SUBMITTER;

export const canReviewScreenings = (role?: Role) => R(role) === BaseRole.STAFF;

export const canProgrammerScreenings = (role?: Role) => R(role) === BaseRole.PROGRAMMER;

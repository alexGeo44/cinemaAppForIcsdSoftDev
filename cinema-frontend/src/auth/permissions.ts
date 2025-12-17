// src/auth/permissions.ts
import { BaseRole } from "../domain/auth/auth.types";

type Role = BaseRole | string | null | undefined;

const normalizeRole = (role: Role): BaseRole | undefined => {
if (role == null) return undefined;

  let r = String(role).trim().toUpperCase();

  // πιάνει και ROLE_ROLE_STAFF κλπ
  while (r.startsWith("ROLE_")) r = r.slice(5);

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
// Όλοι (εκτός ADMIN) έχουν “all USER functions”
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

// ✅ B: όλοι οι ρόλοι (εκτός ADMIN) βλέπουν MyScreenings
export const canViewMyScreenings = (role?: Role) => {
  const r = R(role);
  return (
    r === BaseRole.USER ||
    r === BaseRole.SUBMITTER ||
    r === BaseRole.STAFF ||
    r === BaseRole.PROGRAMMER
  );
};

export const canReviewScreenings = (role?: Role) => R(role) === BaseRole.STAFF;

export const canProgrammerScreenings = (role?: Role) => R(role) === BaseRole.PROGRAMMER;

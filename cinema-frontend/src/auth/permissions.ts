// src/auth/permissions.ts
import { BaseRole } from "../domain/auth/auth.types";
import { normalizeRole } from "./role";

export type Role = BaseRole | string | null | undefined;

// --------------------
// GLOBAL ROLES (base)
// --------------------
export const isAdmin = (role?: Role) => normalizeRole(role) === BaseRole.ADMIN;
export const isUser = (role?: Role) => normalizeRole(role) === BaseRole.USER;

// Cinema domain user == USER only (per spec)
export const isCinemaUser = (role?: Role) => isUser(role);

// --------------------
// PROGRAM-SCOPED HELPERS
// (derived from Program DTO fields)
// --------------------
export type ProgramLike = {
programmerIds?: number[];
programmers?: number[];
staffIds?: number[];
staff?: number[];
creatorUserId?: number;
state?: string;
};

export function getProgrammerIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const ids = (p.programmerIds ?? p.programmers ?? []).filter(
    (x): x is number => typeof x === "number"
  );

  // creator is always a programmer by spec; include if present
  if (typeof p.creatorUserId === "number" && !ids.includes(p.creatorUserId)) {
    return [...ids, p.creatorUserId];
  }
  return ids;
}

export function getStaffIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  return (p.staffIds ?? p.staff ?? []).filter((x): x is number => typeof x === "number");
}

export function isProgrammerOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getProgrammerIds(p).includes(userId);
}

export function isStaffOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getStaffIds(p).includes(userId);
}

export function isCreatorOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return typeof p.creatorUserId === "number" && p.creatorUserId === userId;
}

// --------------------
// PROGRAM ACTIONS (scoped)
// --------------------

// Create program: base-level (cinema USER only)
export const canCreateProgram = (role?: Role) => isCinemaUser(role);

// Manage program (edit / state change / add staff/programmer):
// only programmers of THAT program
export function canManageProgramInProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  return isProgrammerOfProgram(p, userId);
}

// Spec: updates must be completed before ANNOUNCED
export function canEditProgramByState(p: ProgramLike | null | undefined) {
  const st = String(p?.state ?? "");
  return st !== "ANNOUNCED";
}

// --------------------
// SCREENINGS ACTIONS (base + scoped)
// --------------------

// View "My Screenings": cinema USER only
export const canViewMyScreenings = (role?: Role) => isCinemaUser(role);

/**
 * ✅ FINAL RULE for Create Screening (as you requested):
 * - cinema USER can create screenings (in any program)
 * - ADMIN cannot create screenings
 * - ONLY the CREATOR of the program is blocked from creating screenings in that same program
 *
 * Note: if program is not loaded (p == null), we don't block UI; backend should enforce.
 */
export function canCreateScreeningInProgram(
  role: Role | undefined,
  p: ProgramLike | null | undefined,
  userId?: number | null
) {
  if (!userId) return false;
  if (isAdmin(role)) return false;
  if (!isCinemaUser(role)) return false;

  if (!p) return true;
  return !isCreatorOfProgram(p, userId);
}

// If somewhere you still import a "global" canCreateScreening(role),
// keep this for backwards compatibility:
export const canCreateScreening = (role?: Role) => isCinemaUser(role) && !isAdmin(role);

// --------------------
// SCREENING-SCOPED HELPERS
// --------------------
export type ScreeningLike = {
  submitterId?: number | null;
  staffMemberId?: number | null;
  state?: string;
};

export function isOwnerOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.submitterId === userId;
}

export function isAssignedStaffOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.staffMemberId != null && s.staffMemberId === userId;
}

// src/auth/permissions.ts
import { BaseRole } from "../domain/auth/auth.types";
import { normalizeRole } from "./role";

export type Role = BaseRole | string | null | undefined;

// --------------------
// GLOBAL ROLES
// --------------------
export const isAdmin = (role?: Role) => normalizeRole(role) === BaseRole.ADMIN;
export const isUser = (role?: Role) => normalizeRole(role) === BaseRole.USER;

// Cinema domain user == USER only (per spec)
export const isCinemaUser = (role?: Role) => isUser(role);

// --------------------
// GLOBAL NAV PERMS (role-based only)
// --------------------

// Usually: cinema USER can create programs (admin not part of cinema flows)
export function canCreateProgram(role?: Role) {
  return isCinemaUser(role) && !isAdmin(role);
}

// "Create screening" is program-scoped, but nav uses a simple role check
export function canCreateScreening(role?: Role) {
  return isCinemaUser(role) && !isAdmin(role);
}

export function canViewMyScreenings(role?: Role) {
  return isCinemaUser(role) && !isAdmin(role);
}

// These are NOT truly role-based in your system (staff/programmer are program-scoped),
// but Sidebar/Navbar want a boolean. We show pages to USER; pages enforce inside.
export function canReviewScreenings(role?: Role) {
  return isCinemaUser(role) && !isAdmin(role);
}

export function canProgrammerScreenings(role?: Role) {
  return isCinemaUser(role) && !isAdmin(role);
}

// --------------------
// PROGRAM-SCOPED ROLES (derived from Program DTO fields)
// --------------------
export type ProgramLike = {
  programmerIds?: Array<number | string>;
  programmers?: Array<number | string>;
  staffIds?: Array<number | string>;
  staff?: Array<number | string>;
  creatorUserId?: number | string;
  state?: string;
};

function toNum(x: unknown): number | null {
  const n = typeof x === "string" ? Number(x) : typeof x === "number" ? x : NaN;
  return Number.isFinite(n) ? n : null;
}

export function getProgramCreatorId(p?: ProgramLike | null): number | null {
  if (!p) return null;
  const n = toNum(p.creatorUserId);
  return n ?? null;
}

export function getProgrammerIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const idsRaw = [...(p.programmerIds ?? []), ...(p.programmers ?? [])];
  const ids = idsRaw
    .map(toNum)
    .filter((n): n is number => typeof n === "number");

  // NOTE: creator is a "manager" of the program (for edit/state/add staff)
  const creator = getProgramCreatorId(p);
  if (typeof creator === "number" && !ids.includes(creator)) return [...ids, creator];

  return ids;
}

export function getStaffIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const idsRaw = [...(p.staffIds ?? []), ...(p.staff ?? [])];
  return idsRaw.map(toNum).filter((n): n is number => typeof n === "number");
}

export function isCreatorOfProgram(p?: ProgramLike | null, userId?: number | null) {
  if (!p || !userId) return false;
  const creator = getProgramCreatorId(p);
  return creator != null && creator === userId;
}

// Program "managers" = creator + programmerIds/programmers
export function isProgrammerOfProgram(p?: ProgramLike | null, userId?: number | null) {
  if (!p || !userId) return false;
  return getProgrammerIds(p).includes(userId);
}

export function isStaffOfProgram(p?: ProgramLike | null, userId?: number | null) {
  if (!p || !userId) return false;
  return getStaffIds(p).includes(userId);
}

// --------------------
// PROGRAM ACTIONS (program-scoped)
// --------------------
export function canManageProgramInProgram(p?: ProgramLike | null, userId?: number | null) {
  return isProgrammerOfProgram(p, userId);
}

/**
 * ✅ YOUR RULE:
 * - Any logged-in cinema USER can create screening in a program
 * - BUT: the CREATOR of this program cannot create screening in own program
 * - Admin never creates cinema screenings
 */
export function canCreateScreeningInProgram(
  role: Role | undefined,
  p: ProgramLike | null | undefined,
  userId?: number | null
) {
  if (!userId) return false;
  if (isAdmin(role)) return false;
  if (!isCinemaUser(role)) return false;

  // if program not loaded yet, don't block UI (backend will enforce anyway)
  if (!p) return true;

  // ✅ block ONLY creator
  return !isCreatorOfProgram(p, userId);
}

// --------------------
// SCREENING-SCOPED HELPERS
// --------------------
export type ScreeningLike = {
  submitterId?: number | null;
  staffMemberId?: number | null;
  state?: string;
};

export function isOwnerOfScreening(s?: ScreeningLike | null, userId?: number | null) {
  if (!s || !userId) return false;
  return s.submitterId === userId;
}

export function isAssignedStaffOfScreening(s?: ScreeningLike | null, userId?: number | null) {
  if (!s || !userId) return false;
  return s.staffMemberId != null && s.staffMemberId === userId;
}

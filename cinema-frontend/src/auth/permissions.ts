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
// GLOBAL NAV PERMISSIONS
// (μόνο από base role)
// --------------------
export const canViewMyScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);

// Αν έχεις "New program" μόνο για USER:
export const canCreateProgram = (role?: Role) => isCinemaUser(role) && !isAdmin(role);

// Γενική δυνατότητα create screening (χωρίς program context)
export const canCreateScreening = (role?: Role) => isCinemaUser(role) && !isAdmin(role);

// Links για περιοχές (τα pages κάνουν enforce program-scoped)
export const canProgrammerScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);
export const canReviewScreenings = (role?: Role) => isCinemaUser(role) && !isAdmin(role);

// --------------------
// PROGRAM-SCOPED HELPERS
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
  const n = typeof x === "number" ? x : typeof x === "string" ? Number(x) : NaN;
  return Number.isFinite(n) ? n : null;
}

function uniq(nums: number[]) {
  return Array.from(new Set(nums));
}

export function getProgrammerIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const raw = [...(p.programmerIds ?? []), ...(p.programmers ?? [])];
  const ids = raw.map(toNum).filter((x): x is number => x != null);
  return uniq(ids);
}

export function getStaffIds(p?: ProgramLike | null): number[] {
  if (!p) return [];
  const raw = [...(p.staffIds ?? []), ...(p.staff ?? [])];
  const ids = raw.map(toNum).filter((x): x is number => x != null);
  return uniq(ids);
}

/**
 * ✅ CREATOR == owner του program
 * ΜΟΝΟ αυτός θεωρείται ότι "του ανήκει" το program.
 */
export function isCreatorOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  const cid = toNum(p.creatorUserId);
  return cid != null && cid === userId;
}

/**
 * ✅ PROGRAMMER membership (μόνο από λίστα, ΔΕΝ κάνουμε auto-include τον creator εδώ)
 * Για να έχεις "added programmers" ως programmers στα flows,
 * αλλά να μην τους θεωρείς owners.
 */
export function isProgrammerOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getProgrammerIds(p).includes(userId);
}

export function isStaffOfProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  if (!p || !userId) return false;
  return getStaffIds(p).includes(userId);
}

/**
 * ✅ Program management (edit program, change phase, add staff/programmer)
 * Θες να το κάνει ΜΟΝΟ ο creator.
 */
export function canManageProgramInProgram(p: ProgramLike | null | undefined, userId?: number | null) {
  return isCreatorOfProgram(p, userId);
}

/**
 * ✅ Access to PROGRAMMER area for that program:
 * - creator (συνήθως ο βασικός programmer)
 * - ή added programmer (programmerIds)
 */
export function canAccessProgrammerArea(p: ProgramLike | null | undefined, userId?: number | null) {
  return isCreatorOfProgram(p, userId) || isProgrammerOfProgram(p, userId);
}

/**
 * ✅ RULE: create screening in a program
 * Αυτό είναι που ήθελες να "διορθώσουμε":
 * - οποιοσδήποτε cinema USER μπορεί,
 * - ΜΟΝΟ ο CREATOR του ίδιου program δεν μπορεί να υποβάλει στο "δικό του" program
 * - ADMIN όχι
 *
 * (added programmers = "ξένοι" για αυτόν τον κανόνα)
 */
export function canCreateScreeningInProgram(
  role: Role | undefined,
  p: ProgramLike | null | undefined,
  userId?: number | null
) {
  if (!userId) return false;
  if (isAdmin(role)) return false;
  if (!isCinemaUser(role)) return false;

  // αν δεν έχουμε program ακόμα, ας μην μπλοκάρουμε UI
  if (!p) return true;

  // ΜΠΛΟΚ ΜΟΝΟ για CREATOR
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

export function isOwnerOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.submitterId === userId;
}

export function isAssignedStaffOfScreening(s: ScreeningLike | null | undefined, userId?: number | null) {
  if (!s || !userId) return false;
  return s.staffMemberId != null && s.staffMemberId === userId;
}

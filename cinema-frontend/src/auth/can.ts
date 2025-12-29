// src/auth/can.ts
import { authStore } from "./auth.store";
import { BaseRole, type User } from "@/domain/auth/auth.types";
import type { Program } from "@/domain/programs/program.types";
import type { Screening } from "@/domain/screenings/screening.types";

// --------------------
// global role helpers (only USER | ADMIN exist)
// --------------------
function getCurrentUser(): User | null {
  return authStore.getState().user ?? null;
}

export function isAuthenticated(): boolean {
  return !!getCurrentUser();
}

export function isAdmin(): boolean {
  return getCurrentUser()?.role === BaseRole.ADMIN;
}

// Cinema domain actions are for USER only (ADMIN is NOT a cinema superuser).
export function isCinemaUser(): boolean {
  return getCurrentUser()?.role === BaseRole.USER;
}

export function canManageUsers(): boolean {
  return isAdmin();
}

export function canUseCinemaDomain(): boolean {
  return isCinemaUser();
}

// --------------------
// program-scoped role helpers (computed from data)
// --------------------
export function isProgrammerOf(program?: Program | null): boolean {
  const u = getCurrentUser();
  if (!u || !program) return false;
  return Array.isArray((program as any).programmerIds) && (program as any).programmerIds.includes(u.id);
}

export function isStaffOf(program?: Program | null): boolean {
  const u = getCurrentUser();
  if (!u || !program) return false;
  return Array.isArray((program as any).staffIds) && (program as any).staffIds.includes(u.id);
}

export function isSubmitterOf(screening?: Screening | null): boolean {
  const u = getCurrentUser();
  if (!u || !screening) return false;
  return (screening as any).submitterId === u.id;
}

export function isAssignedStaffFor(screening?: Screening | null): boolean {
  const u = getCurrentUser();
  if (!u || !screening) return false;
  return (screening as any).staffMemberId === u.id;
}

// --------------------
// UI "CAN" helpers (use these in pages/components)
// --------------------

// Program management: only PROGRAMMERs of that program
export function canManagePrograms(program?: Program | null): boolean {
  return canUseCinemaDomain() && isProgrammerOf(program);
}

// Screening review: only assigned STAFF for that screening
export function canReviewScreening(screening?: Screening | null): boolean {
  return canUseCinemaDomain() && isAssignedStaffFor(screening);
}

// Submitter actions: only owner of screening
export function canSubmitOrUpdateOwnScreening(screening?: Screening | null): boolean {
  return canUseCinemaDomain() && isSubmitterOf(screening);
}

// Generic "submit screenings" button (create screening): any cinema USER
export function canCreateScreenings(): boolean {
  return canUseCinemaDomain();
}

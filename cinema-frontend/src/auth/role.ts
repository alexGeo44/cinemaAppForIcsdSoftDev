// src/auth/role.ts
import { BaseRole } from "../domain/auth/auth.types";

type RoleLike = BaseRole | string | null | undefined;

export function normalizeRole(role?: RoleLike): BaseRole | undefined {
  if (role == null) return undefined;

  // κάνε normalize σε string
  let r = String(role).trim().toUpperCase();

  // αφαίρεσε όσα ROLE_ υπάρχουν στην αρχή (μερικές φορές έρχεται ROLE_ROLE_X)
  while (r.startsWith("ROLE_")) r = r.slice(5);

  // valid enum check
  return (Object.values(BaseRole) as string[]).includes(r) ? (r as BaseRole) : undefined;
}

// χρήσιμο όταν θες boolean checks
export function hasRole(userRole: RoleLike, allowed: BaseRole | BaseRole[]) {
  const r = normalizeRole(userRole);
  if (!r) return false;
  return Array.isArray(allowed) ? allowed.includes(r) : r === allowed;
}

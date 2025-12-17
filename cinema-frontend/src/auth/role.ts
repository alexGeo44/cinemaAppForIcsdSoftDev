// src/auth/role.ts
import { BaseRole } from "../domain/auth/auth.types";

export function normalizeRole(role?: BaseRole | string | null): BaseRole | undefined {
  if (!role) return undefined;

  const r = String(role).trim().toUpperCase().replace(/^ROLE_/, "");
  // map string -> enum value
  return (Object.values(BaseRole) as string[]).includes(r) ? (r as BaseRole) : undefined;
}

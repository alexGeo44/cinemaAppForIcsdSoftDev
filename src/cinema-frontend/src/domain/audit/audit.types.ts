// src/domain/audit/audit.types.ts
export interface AuditLog {
    actorUserId: number;
action: string;
target: string;
timestamp: string; // ISO string από backend
}

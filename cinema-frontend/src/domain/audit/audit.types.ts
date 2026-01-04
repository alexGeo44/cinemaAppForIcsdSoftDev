export interface AuditLog {
    actorUserId: number | null; // ✅ μπορεί να γίνει null μετά από delete
action: string;
target: string;
timestamp: string;
}

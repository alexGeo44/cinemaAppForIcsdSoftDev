// src/api/audit.api.ts
import axios from "./axios";
import type { AuditLog } from "../domain/audit/audit.types";

export const auditApi = {
list: () => axios.get<AuditLog[]>("/api/admin/audit-logs"),
};

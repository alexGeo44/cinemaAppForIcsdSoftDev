// src/domain/users/user.types.ts
import { BaseRole } from "../auth/auth.types";

export interface User {
id: number;
userName: string;
fullName: string;
role: BaseRole | string;
active: boolean;
}

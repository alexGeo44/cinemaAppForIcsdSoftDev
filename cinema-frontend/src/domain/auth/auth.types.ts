// src/domain/auth/auth.types.ts

// Global/base roles from backend
export enum BaseRole {
    USER = "USER",
    ADMIN = "ADMIN",
}

export interface UserResponse {
    id: number;
userName: string;
fullName: string;
role: BaseRole; // backend returns "USER" | "ADMIN"
active: boolean;
}

export type User = UserResponse;

export interface AuthResponse {
token: string;
user: UserResponse;
}

// Token validation endpoint is used to validate "token belongs to requester"
// Backend side typically returns something like: { userId, role, valid, expired, owner } or similar.
// We'll keep it flexible but still typed.
export interface TokenInfoResponse {
userId: number;
role: BaseRole;
valid?: boolean;
expired?: boolean;
owner?: boolean;
message?: string;
}

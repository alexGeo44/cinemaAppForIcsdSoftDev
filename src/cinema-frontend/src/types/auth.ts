export type PermanentRole = "USER" | "ADMIN";

export interface LoginRequest {
    username: string;
password: string;
}

export interface LoginResponse {
token: string;
user: UserInfo;
}

export interface UserInfo {
id: number;
username: string;
fullName: string;
permanentRole: PermanentRole;
active: boolean;
// ίσως έχεις κι άλλα fields
}

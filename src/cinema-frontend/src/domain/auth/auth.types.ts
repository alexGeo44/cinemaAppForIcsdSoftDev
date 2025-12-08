export interface UserResponse {
    id: number;
userName: string;
fullName: string;
role: BaseRole;
}

export interface AuthResponse {
token: string;
user: UserResponse;
}

export interface TokenInfoResponse {
userId: number;
role: BaseRole;
}

export enum BaseRole {
VISITOR = "VISITOR",
USER = "USER",
PROGRAMMER = "PROGRAMMER",
STAFF = "STAFF",
SUBMITTER = "SUBMITTER",
ADMIN = "ADMIN",
}

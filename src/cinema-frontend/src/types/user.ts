export type UserRole = "ADMIN" | "STAFF" | "PROGRAMMER" | "USER";

export interface User {
    id: number;
username: string;
fullName: string;
baseRole: UserRole;
}

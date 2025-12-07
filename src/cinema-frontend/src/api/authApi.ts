import { http } from "./http";

export interface LoginPayload {
username: string;
password: string;
}

export interface RegisterPayload {
fullName: string;
username: string;
password: string;
}

export interface AuthUser {
id: number;
username: string;
fullName: string;
baseRole: string;
}

export interface AuthResponse {
token: string;
user: AuthUser;
}

export const authApi = {
async register(payload: RegisterPayload): Promise<AuthUser> {
    const res = await http.post("/api/auth/register", payload);
    return res.data as AuthUser;
  },

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const res = await http.post("/api/auth/login", payload);
    return res.data as AuthResponse;
  },
};

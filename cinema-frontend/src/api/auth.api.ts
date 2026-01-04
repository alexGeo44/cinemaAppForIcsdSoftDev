// src/api/auth.api.ts
import axios from "./axios";
import type {
AuthResponse,
UserResponse,
TokenInfoResponse,
} from "../domain/auth/auth.types";

export const authApi = {
login: (username: string, password: string) =>
    axios.post<AuthResponse>("/api/auth/login", { username, password }),

  register: (username: string, password: string, fullName: string) =>
    axios.post<UserResponse>("/api/auth/register", { username, password, fullName }),

  logout: () => axios.post<void>("/api/auth/logout", null),

  me: () => axios.get<UserResponse>("/api/me"),

  // ✅ matches backend: /api/auth/validate?userId=...
  validate: (userId: number) =>
    axios.get<TokenInfoResponse>("/api/auth/validate", { params: { userId } }),
};

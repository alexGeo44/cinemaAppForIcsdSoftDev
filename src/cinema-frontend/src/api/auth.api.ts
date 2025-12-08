import axios from "./axios";
import { AuthResponse, UserResponse, TokenInfoResponse } from "../domain/auth/auth.types";

export const authApi = {
login: (username: string, password: string) =>
    axios.post<AuthResponse>("/api/auth/login", { username, password }),

  register: (username: string, password: string, fullName: string) =>
    axios.post<UserResponse>("/api/auth/register", { username, password, fullName }),

  logout: () =>
    axios.post<void>("/api/auth/logout"),

  me: () =>
    axios.get<UserResponse>("/api/auth/me"),

  validate: () =>
    axios.get<TokenInfoResponse>("/api/auth/validate"),
};

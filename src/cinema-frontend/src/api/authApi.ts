// src/api/authApi.ts
import axiosClient from "./axiosClient";

export type UserResponse = {
id: number;
userName: string;
fullName: string;
role: string;
};

export type AuthResponse = {
token: string;
user: UserResponse;
};

export type LoginRequest = {
username: string;
password: string;
};

export type RegisterUserRequest = {
username: string;
password: string;
fullName: string;
};

export const authApi = {
login: (data: LoginRequest) =>
    axiosClient.post<AuthResponse>("/auth/login", data).then(r => r.data),

  register: (data: RegisterUserRequest) =>
    axiosClient.post<UserResponse>("/auth/register", data).then(r => r.data),

  logout: () =>
    axiosClient.post<void>("/auth/logout"),

  me: () =>
    axiosClient.get<UserResponse>("/auth/me").then(r => r.data),
};

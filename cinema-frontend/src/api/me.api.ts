// src/api/me.api.ts
import axios from "./axios";
import type { UserResponse } from "../domain/auth/auth.types";

export type ChangePasswordPayload = {
oldPassword: string;
newPassword: string;
newPasswordRepeat: string;
};

export const meApi = {
me: () => axios.get<UserResponse>("/api/me"),

  changePassword: (data: ChangePasswordPayload) =>
    axios.put<void>("/api/me/password", data),

  deactivate: () => axios.put<void>("/api/me/deactivate", null),

  deleteMe: () => axios.delete<void>("/api/me"),

  logout: () => axios.post<void>("/api/me/logout"),
};

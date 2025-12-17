// src/api/me.api.ts
import axios from "./axios";
import type { UserResponse } from "../domain/auth/auth.types";

export type ChangePasswordPayload = {
oldPassword: string;
newPassword: string;
newPasswordRepeat: string;
};

export const meApi = {
// ✅ profile/me (authenticated)
me: () => axios.get<UserResponse>("/api/me"),

  // ✅ JSON body
  changePassword: (data: ChangePasswordPayload) =>
    axios.put<void>("/api/me/password", data),

  // ✅ no body needed
  deactivate: () => axios.put<void>("/api/me/deactivate", null),

  // ✅ delete current account
  deleteMe: () => axios.delete<void>("/api/me"),

  // ✅ logout (invalidate token server-side if you support it)
  logout: () => axios.post<void>("/api/me/logout", null),
};

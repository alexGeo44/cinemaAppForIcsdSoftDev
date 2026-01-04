// src/api/users.api.ts
import axios from "./axios";
import { User } from "../domain/users/user.types";

export const usersApi = {
// ✅ Admin list (ταιριάζει με AdminUsersController: /api/admin/users)
list: () => axios.get<User[]>("/api/admin/users"),

  // ✅ My account: change password (MeController: PUT /api/me/password)
  changePassword: (data: { oldPassword: string; newPassword: string; newPasswordRepeat: string }) =>
    axios.put<void>("/api/me/password", data),

  // ✅ My account: deactivate (MeController: PUT /api/me/deactivate)
  deactivateMe: () => axios.put<void>("/api/me/deactivate", null),

  // ✅ My account: delete (MeController: DELETE /api/me)
  deleteMe: () => axios.delete<void>("/api/me"),

  // ✅ Admin actions (AdminUsersController)
  activate: (id: number) => axios.put<void>(`/api/admin/users/${id}/activate`, null),
  deactivate: (id: number) => axios.put<void>(`/api/admin/users/${id}/deactivate`, null),
  delete: (id: number) => axios.delete<void>(`/api/admin/users/${id}`),
};

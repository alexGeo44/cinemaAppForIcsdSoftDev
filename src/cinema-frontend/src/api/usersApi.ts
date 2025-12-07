import axiosClient from "./axiosClient";
import type { UserResponse } from "../types";

export const usersApi = {
// αν έχεις GET /api/users:
list: () => axiosClient.get<UserResponse[]>("/users").then((r) => r.data),

  deactivate: (id: number) =>
    axiosClient.put<void>(`/users/${id}/deactivate`).then((r) => r.data),

  delete: (id: number) =>
    axiosClient.delete<void>(`/users/${id}`).then((r) => r.data),
};

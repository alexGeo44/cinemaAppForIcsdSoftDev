import axios from "./axios";
import { User } from "../domain/auth/auth.types";

export const usersApi = {

list: () =>
    axios.get<User[]>("/api/users"),


  register: (data: {
    username: string;
    password: string;
    fullName: string;
  }) =>
    axios.post<void>("/api/users", data),


  changePassword: (
    userId: number,
    data: { oldPassword: string; newPassword: string }
  ) =>
    axios.put<void>(`/api/users/${userId}/password`, data),


  deactivate: (userId: number) =>
    axios.put<void>(`/api/users/${userId}/deactivate`),


  delete: (userId: number) =>
    axios.delete<void>(`/api/users/${userId}`),
};

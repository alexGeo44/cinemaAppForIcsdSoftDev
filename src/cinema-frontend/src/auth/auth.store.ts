import { create } from "zustand";
import { UserResponse } from "../domain/auth/auth.types";
import { authApi } from "../api/auth.api";

interface AuthState {
token: string | null;
user: UserResponse | null;
login: (u: string, p: string) => Promise<void>;
  logout: () => void;
  loadMe: () => Promise<void>;
}

export const authStore = create<AuthState>((set) => ({
  token: localStorage.getItem("token"),
  user: null,

  login: async (username, password) => {
    const { data } = await authApi.login(username, password);
    localStorage.setItem("token", data.token);
    set({ token: data.token, user: data.user });
  },

  logout: () => {
    localStorage.removeItem("token");
    set({ token: null, user: null });
  },

  loadMe: async () => {
    const { data } = await authApi.me();
    set({ user: data });
  },
}));

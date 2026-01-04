import { create } from "zustand";
import type { UserResponse, BaseRole } from "../domain/auth/auth.types";
import { authApi } from "../api/auth.api";
import { normalizeRole } from "./role";
import { tokenStorage } from "./tokenStorage";

interface AuthState {
token: string | null;
user: UserResponse | null;
bootstrapped: boolean;

login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  loadMe: () => Promise<void>;

  hasRole: (role: BaseRole | BaseRole[]) => boolean;
}

export const authStore = create<AuthState>((set, get) => ({
  token: tokenStorage.get(),
  user: null,
  bootstrapped: false,

  login: async (username, password) => {
    const { data } = await authApi.login(username, password);

    tokenStorage.set(data.token);

    const role = normalizeRole((data.user as any)?.role);

    set({
      token: data.token,
      user: { ...(data.user as any), role: role ?? (data.user as any).role },
      bootstrapped: true,
    });
  },

  logout: async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore - token might already be invalid/expired
    }
    tokenStorage.clear();
    set({ token: null, user: null, bootstrapped: true });
  },

  loadMe: async () => {
    if (get().bootstrapped) return;

    const token = tokenStorage.get();
    if (!token) {
      set({ token: null, user: null, bootstrapped: true });
      return;
    }

    try {
      // 1) get current user
      const { data: me } = await authApi.me();
      const role = normalizeRole((me as any)?.role);

      const user: UserResponse = { ...(me as any), role: role ?? (me as any).role };

      // 2) validate that token belongs to requester userId (spec)
      // If invalid/expired/not-owner => backend returns error; we clear token.
      await authApi.validate(user.id);

      set({
        token,
        user,
        bootstrapped: true,
      });
    } catch {
      tokenStorage.clear();
      set({ token: null, user: null, bootstrapped: true });
    }
  },

  hasRole: (role) => {
    const u = get().user;
    if (!u) return false;

    const userRole = normalizeRole((u as any).role);
    if (!userRole) return false;

    return Array.isArray(role) ? role.includes(userRole) : userRole === role;
  },
}));

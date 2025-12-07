import React, { createContext, useContext, useEffect, useState } from "react";
import http, { setAuthToken } from "../api/http";
import type { User, UserRole } from "../types/user";

interface AuthState {
  user: User | null;
  token: string | null;
}

interface LoginPayload {
  username: string;
  password: string;
}

interface AuthResponse {
  token: string;
  user: User;
}

interface AuthContextValue extends AuthState {
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const STORAGE_KEY = "cinema-auth";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>({ user: null, token: null });

  // Φόρτωσε από localStorage στην αρχή
  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    try {
      const parsed: AuthState = JSON.parse(raw);
      setState(parsed);
      setAuthToken(parsed.token);
    } catch {
      // ignore
    }
  }, []);

  const login = async (payload: LoginPayload) => {
    const res = await http.post<AuthResponse>("/api/auth/login", payload);
    const { token, user } = res.data;
    const newState: AuthState = { token, user };
    setState(newState);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newState));
    setAuthToken(token);
  };

  const logout = () => {
    setState({ user: null, token: null });
    localStorage.removeItem(STORAGE_KEY);
    setAuthToken(null);
  };

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};

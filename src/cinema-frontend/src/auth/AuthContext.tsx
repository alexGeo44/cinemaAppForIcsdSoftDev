import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import { authApi } from "../api/authApi";

// 🔴 ΠΡΙΝ:
// import {
//   AuthResponse,
//   LoginRequest,
//   UserResponse,
// } from "../types";

// 🟢 ΜΕΤΑ:
import type {
  AuthResponse,
  LoginRequest,
  UserResponse,
} from "../types";

interface AuthContextValue {
  user: UserResponse | null;
  token: string | null;
  loading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  token: null,
  loading: true,
  login: async () => {},
  logout: async () => {},
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("authToken")
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      if (token) {
        try {
          const me = await authApi.me();
          setUser(me);
        } catch {
          setUser(null);
          setToken(null);
          localStorage.removeItem("authToken");
        }
      }
      setLoading(false);
    };
    init();
  }, [token]);

  const login = async (data: LoginRequest) => {
    const res: AuthResponse = await authApi.login(data);
    setToken(res.token);
    localStorage.setItem("authToken", res.token);
    setUser(res.user);
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {}
    setUser(null);
    setToken(null);
    localStorage.removeItem("authToken");
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

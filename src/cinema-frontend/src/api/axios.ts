import axios from "axios";
import { authStore } from "../auth/auth.store";

const instance = axios.create({
baseURL: "http://localhost:8080",
withCredentials: true,
});

instance.interceptors.request.use((config) => {
  const token = authStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

instance.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      authStore.getState().logout();
    }
    return Promise.reject(err);
  }
);

export default instance;

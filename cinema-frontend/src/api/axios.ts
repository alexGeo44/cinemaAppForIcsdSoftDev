import axios from "axios";
import { tokenStorage } from "../auth/tokenStorage";

const instance = axios.create({ baseURL: "http://localhost:8080" });

instance.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers = { ...(config.headers || {}), Authorization: `Bearer ${token}` };
  }
  return config;
});

export default instance;

import axios from "axios";
import { authStorage } from "../auth/auth-storage";
import { apiBaseUrl } from "../config/env";

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10_000,
  headers: { Accept: "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const session = authStorage.get();
  if (session) {
    config.headers.Authorization = `${session.tokenType} ${session.accessToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const requestUrl = error.config?.url ?? "";
      if (
        !requestUrl.includes("/auth/login") &&
        !requestUrl.includes("/auth/register")
      ) {
        authStorage.clear();
        window.dispatchEvent(new CustomEvent("sicms:auth-expired"));
      }
    }
    return Promise.reject(error);
  },
);

const defaultApiBaseUrl = "http://localhost:8080/api";

function normalizeBaseUrl(value: string): string {
  return value.replace(/\/+$/, "");
}

export const apiBaseUrl = normalizeBaseUrl(
  import.meta.env.VITE_API_BASE_URL?.trim() || defaultApiBaseUrl,
);

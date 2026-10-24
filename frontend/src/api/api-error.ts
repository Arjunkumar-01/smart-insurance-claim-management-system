import axios from "axios";

export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}

export type ApiErrorKind =
  | "validation"
  | "unauthorized"
  | "forbidden"
  | "not-found"
  | "conflict"
  | "server"
  | "network"
  | "unknown";

export class ApiError extends Error {
  readonly kind: ApiErrorKind;
  readonly status?: number;
  readonly fieldErrors: Record<string, string>;
  readonly path?: string;

  constructor(
    message: string,
    options: {
      kind: ApiErrorKind;
      status?: number;
      fieldErrors?: Record<string, string>;
      path?: string;
    },
  ) {
    super(message);
    this.name = "ApiError";
    this.kind = options.kind;
    this.status = options.status;
    this.fieldErrors = options.fieldErrors ?? {};
    this.path = options.path;
  }
}

function kindForStatus(status?: number): ApiErrorKind {
  if (status === 400) return "validation";
  if (status === 401) return "unauthorized";
  if (status === 403) return "forbidden";
  if (status === 404) return "not-found";
  if (status === 409) return "conflict";
  if (status !== undefined && status >= 500) return "server";
  return "unknown";
}

export function normalizeApiError(error: unknown): ApiError {
  if (!axios.isAxiosError<ApiErrorPayload>(error)) {
    return new ApiError("An unexpected error occurred.", { kind: "unknown" });
  }

  if (!error.response) {
    return new ApiError(
      "The service could not be reached. Check your connection and try again.",
      { kind: "network" },
    );
  }

  const payload = error.response.data;
  const status = error.response.status;
  const message = payload?.message || "The request could not be completed.";

  return new ApiError(message, {
    kind: kindForStatus(status),
    status,
    fieldErrors: payload?.fieldErrors,
    path: payload?.path,
  });
}

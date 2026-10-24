import axios from "axios";
import { describe, expect, it } from "vitest";
import { ApiError, normalizeApiError } from "./api-error";

describe("normalizeApiError", () => {
  it("preserves safe backend validation details", () => {
    const error = new axios.AxiosError(
      "request failed",
      "ERR_BAD_REQUEST",
      undefined,
      undefined,
      {
        status: 400,
        statusText: "Bad Request",
        headers: {},
        config: {},
        data: {
          message: "Request validation failed",
          fieldErrors: { email: "Email must be valid" },
        },
      },
    );
    const normalized = normalizeApiError(error);
    expect(normalized).toBeInstanceOf(ApiError);
    expect(normalized.kind).toBe("validation");
    expect(normalized.message).toBe("Request validation failed");
    expect(normalized.fieldErrors.email).toBe("Email must be valid");
  });

  it("normalizes network failures without exposing raw errors", () => {
    const normalized = normalizeApiError(new axios.AxiosError("ECONNREFUSED"));
    expect(normalized.kind).toBe("network");
    expect(normalized.message).toContain("service could not be reached");
  });
});

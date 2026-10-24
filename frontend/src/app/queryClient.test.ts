import { describe, expect, it } from "vitest";
import { queryClient } from "../api/query-client";

describe("query client foundation", () => {
  it("does not retry mutations and avoids retrying normal client errors", () => {
    const defaults = queryClient.getDefaultOptions();
    expect(defaults.mutations?.retry).toBe(false);
    expect(defaults.queries?.retry?.(0, { response: { status: 401 } })).toBe(
      false,
    );
    expect(defaults.queries?.retry?.(0, { response: { status: 500 } })).toBe(
      true,
    );
  });
});

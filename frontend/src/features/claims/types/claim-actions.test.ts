import { describe, expect, it } from "vitest";
import { getAvailableClaimActions } from "./claim-actions";

describe("claim workflow action policy", () => {
  it("exposes only valid actions for each status", () => {
    expect(getAvailableClaimActions("SUBMITTED")).toEqual(["START_REVIEW"]);
    expect(getAvailableClaimActions("UNDER_REVIEW")).toEqual([
      "REQUEST_DOCUMENTS",
      "APPROVE",
      "REJECT",
    ]);
    expect(getAvailableClaimActions("DOCUMENTS_REQUIRED")).toEqual([
      "RESUME_REVIEW",
    ]);
    expect(getAvailableClaimActions("APPROVED")).toEqual(["SETTLE"]);
    expect(getAvailableClaimActions("REJECTED")).toEqual([]);
    expect(getAvailableClaimActions("SETTLED")).toEqual([]);
  });
});

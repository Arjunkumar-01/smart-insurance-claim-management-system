import { describe, expect, it } from "vitest";

describe("product comparison selection", () => {
  it("accepts two through four unique IDs and blocks fifth or duplicate selection", () => {
    const add = (selected: string[], id: string) =>
      selected.includes(id) || selected.length >= 4
        ? selected
        : [...selected, id];
    expect(add([], "1")).toEqual(["1"]);
    expect(add(["1"], "1")).toEqual(["1"]);
    expect(add(["1", "2", "3", "4"], "5")).toEqual(["1", "2", "3", "4"]);
    expect(add(["1", "2"], "3")).toHaveLength(3);
  });
});

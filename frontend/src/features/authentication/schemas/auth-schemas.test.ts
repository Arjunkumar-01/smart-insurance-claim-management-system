import { describe, expect, it } from "vitest";
import { loginSchema, registerSchema } from "./auth-schemas";

describe("authentication schemas", () => {
  it("does not apply registration password complexity to login", () => {
    expect(
      loginSchema.safeParse({ email: "user@example.test", password: "plain" })
        .success,
    ).toBe(true);
  });

  it("mirrors registration password and confirmation rules", () => {
    expect(
      registerSchema.safeParse({
        firstName: "Test",
        lastName: "User",
        email: "user@example.test",
        password: "weak",
        confirmPassword: "weak",
      }).success,
    ).toBe(false);
    expect(
      registerSchema.safeParse({
        firstName: "Test",
        lastName: "User",
        email: "user@example.test",
        password: "Strong!1",
        confirmPassword: "Different!1",
      }).success,
    ).toBe(false);
    expect(
      registerSchema.safeParse({
        firstName: "Test",
        lastName: "User",
        email: "user@example.test",
        password: "Strong!1",
        confirmPassword: "Strong!1",
      }).success,
    ).toBe(true);
  });
});

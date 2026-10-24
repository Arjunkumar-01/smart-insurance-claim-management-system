import type { AuthSession } from "./auth-types";
import { APPLICATION_ROLES } from "../types/roles";

const sessionKey = "sicms.auth.session";

export const authStorage = {
  get(): AuthSession | null {
    const raw = sessionStorage.getItem(sessionKey);
    if (!raw) return null;
    try {
      const parsed: unknown = JSON.parse(raw);
      if (!isAuthSession(parsed)) {
        this.clear();
        return null;
      }
      if (new Date(parsed.expiresAt).getTime() <= Date.now()) {
        this.clear();
        return null;
      }
      return parsed;
    } catch {
      this.clear();
      return null;
    }
  },
  set(session: AuthSession) {
    sessionStorage.setItem(sessionKey, JSON.stringify(session));
  },
  clear() {
    sessionStorage.removeItem(sessionKey);
  },
};

function isAuthSession(value: unknown): value is AuthSession {
  if (typeof value !== "object" || value === null) return false;
  const session = value as Partial<AuthSession>;
  const user = session.user as Partial<AuthSession["user"]> | undefined;
  return (
    typeof session.accessToken === "string" &&
    session.tokenType === "Bearer" &&
    typeof session.expiresAt === "string" &&
    typeof session.user === "object" &&
    session.user !== null &&
    typeof user?.userId === "string" &&
    typeof user.email === "string" &&
    Array.isArray(user.roles) &&
    user.roles.length > 0 &&
    user.roles.every((role) => APPLICATION_ROLES.includes(role))
  );
}

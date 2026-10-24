import { fireEvent, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it } from "vitest";
import { AuthProvider } from "./AuthProvider";
import { authStorage } from "./auth-storage";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleRoute } from "./RoleRoute";
import { useAuth } from "./useAuth";
import { renderWithProviders } from "../test/renderWithProviders";

const customerSession = {
  accessToken: "synthetic-token",
  tokenType: "Bearer" as const,
  expiresAt: new Date(Date.now() + 60_000).toISOString(),
  user: {
    userId: "user-1",
    email: "customer@example.test",
    firstName: "Test",
    lastName: "Customer",
    roles: ["CUSTOMER"] as const,
    accountStatus: "ACTIVE" as const,
    createdAt: new Date().toISOString(),
  },
};

function RouteHarness() {
  return (
    <Routes>
      <Route element={<ProtectedRoute />}>
        <Route element={<RoleRoute allowedRoles={["CUSTOMER"]} />}>
          <Route path="/customer" element={<div>Customer landing</div>} />
        </Route>
      </Route>
      <Route path="/login" element={<div>Login route</div>} />
    </Routes>
  );
}

describe("authentication route guards", () => {
  beforeEach(async () => {
    authStorage.clear();
  });

  it("redirects anonymous users to login", async () => {
    renderWithProviders(
      <AuthProvider>
        <MemoryRouter initialEntries={["/customer"]}>
          <RouteHarness />
        </MemoryRouter>
      </AuthProvider>,
    );
    expect(await screen.findByText("Login route")).toBeInTheDocument();
  });

  it("allows the matching role and renders forbidden for a different role", async () => {
    authStorage.set(customerSession);
    renderWithProviders(
      <AuthProvider>
        <MemoryRouter initialEntries={["/customer"]}>
          <RouteHarness />
        </MemoryRouter>
      </AuthProvider>,
    );
    expect(await screen.findByText("Customer landing")).toBeInTheDocument();

    function ForbiddenHarness() {
      return (
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route element={<RoleRoute allowedRoles={["ADMINISTRATOR"]} />}>
              <Route path="/customer" element={<div>Admin landing</div>} />
            </Route>
          </Route>
        </Routes>
      );
    }
    renderWithProviders(
      <AuthProvider>
        <MemoryRouter initialEntries={["/customer"]}>
          <ForbiddenHarness />
        </MemoryRouter>
      </AuthProvider>,
    );
    await waitFor(() =>
      expect(screen.getByText(/do not have access/i)).toBeInTheDocument(),
    );
  });
});

describe("logout cache isolation", () => {
  it("clears the authenticated query cache", async () => {
    function Probe() {
      const auth = useAuth();
      return <button onClick={auth.logout}>Log out</button>;
    }
    const { queryClient: testQueryClient } = renderWithProviders(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );
    testQueryClient.setQueryData(["private"], { owner: "customer" });
    fireEvent.click(screen.getByRole("button", { name: "Log out" }));
    await waitFor(() =>
      expect(testQueryClient.getQueryData(["private"])).toBeUndefined(),
    );
  });
});

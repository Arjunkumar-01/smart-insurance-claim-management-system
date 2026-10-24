import { screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../auth/AuthProvider";
import { LoginPage } from "../features/authentication/pages/LoginPage";
import { NotFoundPage } from "../routes/NotFoundPage";
import { renderWithProviders } from "../test/renderWithProviders";

describe("application foundation", () => {
  it("renders the public login foundation", () => {
    renderWithProviders(
      <AuthProvider>
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      </AuthProvider>,
    );
    expect(
      screen.getByRole("heading", { name: "Welcome back" }),
    ).toBeInTheDocument();
  });

  it("renders the not-found page without authentication assumptions", () => {
    renderWithProviders(
      <MemoryRouter>
        <NotFoundPage />
      </MemoryRouter>,
    );
    expect(
      screen.getByRole("heading", { name: "Page not found" }),
    ).toBeInTheDocument();
  });
});

import { fireEvent, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import { LoadingState } from "./LoadingState";
import { renderWithProviders } from "../../test/renderWithProviders";

describe("feedback components", () => {
  it("announces loading state accessibly", () => {
    renderWithProviders(<LoadingState />);
    expect(screen.getByRole("status")).toHaveTextContent("Loading");
  });

  it("renders safe error text and retry action", () => {
    const onRetry = vi.fn();
    renderWithProviders(
      <ErrorState message="Please retry" onRetry={onRetry} />,
    );
    expect(screen.getByRole("alert")).toHaveTextContent("Please retry");
    fireEvent.click(screen.getByRole("button", { name: "Try again" }));
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it("renders an empty state", () => {
    renderWithProviders(<EmptyState title="No records" />);
    expect(
      screen.getByRole("heading", { name: "No records" }),
    ).toBeInTheDocument();
  });
});

import { fireEvent, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ConfirmationDialog } from "./ConfirmationDialog";
import { FormSection } from "./FormSection";
import { PageHeader } from "./PageHeader";
import { StatusBadge } from "./StatusBadge";
import { renderWithProviders } from "../../test/renderWithProviders";

describe("shared enterprise UI", () => {
  it("renders a page header and semantic status badge", () => {
    renderWithProviders(
      <>
        <PageHeader
          eyebrow="Workspace"
          title="Operations"
          description="Controlled workspace"
        />
        <StatusBadge label="Under review" tone="info" />
      </>,
    );
    expect(
      screen.getByRole("heading", { name: "Operations" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Under review")).toBeInTheDocument();
  });

  it("renders a form section and confirmation actions accessibly", () => {
    const onCancel = vi.fn();
    const onConfirm = vi.fn();
    renderWithProviders(
      <>
        <FormSection title="Identity" description="Safe account details">
          <span>Fields</span>
        </FormSection>
        <ConfirmationDialog
          open
          title="Confirm action"
          description="This action will continue."
          onCancel={onCancel}
          onConfirm={onConfirm}
        />
      </>,
    );
    expect(screen.getByText("Identity")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Cancel" }));
    fireEvent.click(screen.getByRole("button", { name: "Confirm" }));
    expect(onCancel).toHaveBeenCalledOnce();
    expect(onConfirm).toHaveBeenCalledOnce();
  });
});

import { fireEvent, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { NotificationProvider } from "./NotificationProvider";
import { useNotifications } from "./useNotifications";
import { renderWithProviders } from "../../test/renderWithProviders";

function NotificationTrigger() {
  const { notify } = useNotifications();
  return <button onClick={() => notify("Saved", "success")}>Notify</button>;
}

describe("notification foundation", () => {
  it("renders and dismisses a semantic notification", () => {
    renderWithProviders(
      <NotificationProvider>
        <NotificationTrigger />
      </NotificationProvider>,
    );
    fireEvent.click(screen.getByRole("button", { name: "Notify" }));
    expect(screen.getByRole("status")).toHaveTextContent("Saved");
    fireEvent.click(screen.getByRole("button", { name: "Close" }));
    expect(screen.queryByRole("status")).not.toBeInTheDocument();
  });
});

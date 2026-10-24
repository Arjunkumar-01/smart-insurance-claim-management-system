import { fireEvent, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderWithProviders } from "../../../test/renderWithProviders";
import { paymentApi } from "../api/payment-api";
import { DownloadReceiptButton } from "./DownloadReceiptButton";

vi.mock("../api/payment-api", () => ({
  paymentApi: { downloadReceipt: vi.fn() },
}));

describe("Receipt download", () => {
  beforeEach(() => {
    vi.mocked(paymentApi.downloadReceipt).mockResolvedValue(
      new Blob(["pdf"], { type: "application/pdf" }),
    );
    vi.stubGlobal("URL", {
      createObjectURL: vi.fn(() => "blob:receipt"),
      revokeObjectURL: vi.fn(),
    });
    vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(
      () => undefined,
    );
  });

  it("downloads an authenticated Blob and revokes its object URL", async () => {
    renderWithProviders(
      <DownloadReceiptButton paymentId="payment-1" receiptNumber="REC-1" />,
    );
    fireEvent.click(screen.getByRole("button", { name: "Download receipt" }));
    await screen.findByRole("button", { name: "Download receipt" });
    expect(paymentApi.downloadReceipt).toHaveBeenCalledWith("payment-1");
    expect(URL.createObjectURL).toHaveBeenCalled();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith("blob:receipt");
  });
});

import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { productApi } from "./product-api";

describe("product API", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("constructs supported list filters and sort parameters", async () => {
    const get = vi.spyOn(apiClient, "get").mockResolvedValue({
      data: {
        products: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
    } as never);
    await productApi.list({
      name: "health",
      productCode: "H1",
      category: "HEALTH",
      page: 0,
      size: 20,
      sort: "name,asc",
    });
    expect(get).toHaveBeenCalledWith("/products", {
      params: {
        name: "health",
        productCode: "H1",
        category: "HEALTH",
        page: 0,
        size: 20,
        sort: "name,asc",
      },
    });
  });

  it("constructs detail, comparison, create, and update requests", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: { products: [] } } as never);
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: {} } as never);
    const put = vi
      .spyOn(apiClient, "put")
      .mockResolvedValue({ data: {} } as never);
    await productApi.get("p1");
    await productApi.compare(["p1", "p2"]);
    await productApi.create({
      productCode: "H1",
      name: "Health",
      description: "",
      category: "HEALTH",
      coverageDescription: "Cover",
      coverageAmount: 100,
      premiumAmount: 10,
    });
    await productApi.update("p1", {
      name: "Health",
      description: "",
      category: "HEALTH",
      coverageDescription: "Cover",
      coverageAmount: 100,
      premiumAmount: 10,
      status: "ACTIVE",
    });
    expect(get).toHaveBeenCalledWith("/products/p1");
    expect(get).toHaveBeenCalledWith("/products/compare", {
      params: { ids: "p1,p2" },
    });
    expect(post).toHaveBeenCalledWith(
      "/products",
      expect.objectContaining({ productCode: "H1" }),
    );
    expect(put).toHaveBeenCalledWith(
      "/products/p1",
      expect.objectContaining({ status: "ACTIVE" }),
    );
  });
});

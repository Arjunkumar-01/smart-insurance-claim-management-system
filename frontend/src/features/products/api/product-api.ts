import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  CreateProductRequest,
  ProductComparisonResponse,
  ProductFilters,
  ProductPageResponse,
  ProductResponse,
  UpdateProductRequest,
} from "../types/product-types";

export const productKeys = {
  all: ["products"] as const,
  list: (filters: ProductFilters) =>
    [...productKeys.all, "list", filters] as const,
  detail: (id: string) => [...productKeys.all, "detail", id] as const,
  compare: (ids: string[]) => [...productKeys.all, "compare", ids] as const,
};

async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}

export const productApi = {
  list: (filters: ProductFilters) => {
    const params = Object.fromEntries(
      Object.entries(filters).filter(
        ([, value]) => value !== undefined && value !== "",
      ),
    );
    return request<ProductPageResponse>(() =>
      apiClient.get("/products", { params }),
    );
  },
  get: (id: string) =>
    request<ProductResponse>(() => apiClient.get(`/products/${id}`)),
  compare: (ids: string[]) =>
    request<ProductComparisonResponse>(() =>
      apiClient.get("/products/compare", { params: { ids: ids.join(",") } }),
    ),
  create: (payload: CreateProductRequest) =>
    request<ProductResponse>(() => apiClient.post("/products", payload)),
  update: (id: string, payload: UpdateProductRequest) =>
    request<ProductResponse>(() => apiClient.put(`/products/${id}`, payload)),
};

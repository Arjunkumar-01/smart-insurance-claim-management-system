import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  AdminUser,
  AdminUserPage,
  ChangeAccountStatusRequest,
} from "../types/admin-types";
export const adminUserKeys = {
  all: ["admin-users"] as const,
  list: (page: number, sort: string) =>
    [...adminUserKeys.all, "list", page, sort] as const,
  detail: (id: string) => [...adminUserKeys.all, "detail", id] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
export const adminApi = {
  list: (page = 0, size = 20, sort = "createdAt") =>
    request<AdminUserPage>(() =>
      apiClient.get("/admin/users", { params: { page, size, sort } }),
    ),
  detail: (id: string) =>
    request<AdminUser>(() => apiClient.get(`/admin/users/${id}`)),
  changeStatus: (id: string, payload: ChangeAccountStatusRequest) =>
    request<AdminUser>(() =>
      apiClient.patch(`/admin/users/${id}/status`, payload),
    ),
};

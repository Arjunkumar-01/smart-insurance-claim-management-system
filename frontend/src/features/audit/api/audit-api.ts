import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type { AuditPage } from "../types/audit-types";
export const auditKeys = {
  all: ["audit"] as const,
  list: (page: number) => [...auditKeys.all, page] as const,
};
export async function getAuditLogs(page = 0, size = 20): Promise<AuditPage> {
  try {
    return (
      await apiClient.get<AuditPage>("/admin/audit-logs", {
        params: { page, size },
      })
    ).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}

export interface AuditLog {
  auditId: string;
  action: string;
  actorId: string | null;
  resourceType: string;
  resourceId: string | null;
  description: string;
  occurredAt: string;
}
export interface AuditPage {
  logs: AuditLog[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

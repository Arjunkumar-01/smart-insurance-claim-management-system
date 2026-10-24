export type AccountStatus = "ACTIVE" | "INACTIVE" | "LOCKED";
export interface AdminUser {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  accountStatus: AccountStatus;
  createdAt: string;
  updatedAt: string;
}
export interface AdminUserPage {
  users: AdminUser[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ChangeAccountStatusRequest {
  status: AccountStatus;
}

export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  role: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CreateUserRequest extends LoginRequest {}

export interface AdminUser extends User {
  enabled: boolean;
  createdAt: string;
}

export type UserSortField = 'username' | 'role' | 'enabled' | 'createdAt' | 'id';
export type SortDirection = 'asc' | 'desc';

export interface UserListQuery {
  search?: string;
  page?: number;
  size?: number;
  sort?: UserSortField;
  direction?: SortDirection;
}

export interface UpdateUserRequest {
  username: string;
  password?: string;
  enabled: boolean;
}

export interface TokenResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export interface StoredSession extends TokenResponse {}

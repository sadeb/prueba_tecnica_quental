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

export interface RegisterRequest extends LoginRequest {}

export interface TokenResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export interface StoredSession extends TokenResponse {}

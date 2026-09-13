export interface Session {
  readonly token: string;
  readonly username: string;
  /** ISO-8601 instant issued by the backend. */
  readonly expiresAt: string;
}

export interface LoginRequest {
  readonly username: string;
  readonly password: string;
}

export interface LoginResponse {
  readonly token: string;
  readonly expiresAt: string;
  readonly username: string;
}

export interface RegisterResponse {
  readonly id: number;
  readonly username: string;
}

export function isSession(value: unknown): value is Session {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate['token'] === 'string' &&
    candidate['token'].length > 0 &&
    typeof candidate['username'] === 'string' &&
    typeof candidate['expiresAt'] === 'string'
  );
}

/** An unparsable `expiresAt` is treated as "not expired": the backend 401 is the final word. */
export function isSessionExpired(session: Session, now: number = Date.now()): boolean {
  const expiry = Date.parse(session.expiresAt);
  return !Number.isNaN(expiry) && expiry <= now;
}

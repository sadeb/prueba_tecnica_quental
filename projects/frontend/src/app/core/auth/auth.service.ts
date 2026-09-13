import { HttpClient } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  isSessionExpired,
  LoginRequest,
  LoginResponse,
  RegisterResponse,
  Session,
} from './session.model';
import { TokenStorage } from './token.storage';

export type SessionStatus = 'valid' | 'expired' | 'none';

/**
 * Owns the session: login/register HTTP calls, token persistence and the derived signals the
 * rest of the app reads. Navigation is deliberately left to callers (guards, navbar, interceptor).
 */
@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorage);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly sessionSignal = signal<Session | null>(this.restoreSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.sessionSignal() !== null);
  readonly username = computed(() => this.sessionSignal()?.username ?? null);
  readonly token = computed(() => this.sessionSignal()?.token ?? null);

  login(credentials: LoginRequest): Observable<Session> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, credentials).pipe(
      map(
        (response): Session => ({
          token: response.token,
          username: response.username,
          expiresAt: response.expiresAt,
        }),
      ),
      tap((session) => this.startSession(session)),
    );
  }

  register(credentials: LoginRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, credentials);
  }

  logout(): void {
    this.tokenStorage.clear();
    this.sessionSignal.set(null);
  }

  /** Checks expiry at call time (guards, interceptor); an expired session is cleared on the spot. */
  sessionStatus(): SessionStatus {
    const session = this.sessionSignal();
    if (session === null) {
      return 'none';
    }
    if (isSessionExpired(session)) {
      this.logout();
      return 'expired';
    }
    return 'valid';
  }

  hasValidSession(): boolean {
    return this.sessionStatus() === 'valid';
  }

  private startSession(session: Session): void {
    this.tokenStorage.write(session);
    this.sessionSignal.set(session);
  }

  private restoreSession(): Session | null {
    const stored = this.tokenStorage.read();
    if (stored !== null && isSessionExpired(stored)) {
      this.tokenStorage.clear();
      return null;
    }
    return stored;
  }
}

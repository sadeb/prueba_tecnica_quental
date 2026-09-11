import { computed, Injectable, signal } from '@angular/core';
import { StoredSession } from '../models/auth.models';

const SESSION_KEY = 'quental.rickmorty.session';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly sessionState = signal<StoredSession | null>(this.readSession());
  private expirationTimer: ReturnType<typeof setTimeout> | null = null;

  readonly session = this.sessionState.asReadonly();
  readonly user = computed(() => this.sessionState()?.user ?? null);
  readonly token = computed(() => this.sessionState()?.token ?? null);
  readonly isAuthenticated = computed(() => this.sessionState() !== null);
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');

  constructor() {
    this.scheduleExpiration(this.sessionState());
  }

  setSession(session: StoredSession): void {
    this.sessionState.set(session);
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.scheduleExpiration(session);
  }

  clear(): void {
    this.sessionState.set(null);
    localStorage.removeItem(SESSION_KEY);
    if (this.expirationTimer) clearTimeout(this.expirationTimer);
    this.expirationTimer = null;
  }

  private readSession(): StoredSession | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) return null;
      const session = JSON.parse(raw) as StoredSession;
      if (!session.token || !session.user || Date.parse(session.expiresAt) <= Date.now()) {
        localStorage.removeItem(SESSION_KEY);
        return null;
      }
      return session;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private scheduleExpiration(session: StoredSession | null): void {
    if (this.expirationTimer) clearTimeout(this.expirationTimer);
    if (!session) return;
    const remaining = Math.max(0, Date.parse(session.expiresAt) - Date.now());
    this.expirationTimer = setTimeout(() => this.clear(), Math.min(remaining, 2_147_483_647));
  }
}

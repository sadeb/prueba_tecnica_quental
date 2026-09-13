import { inject, Service } from '@angular/core';
import { BrowserStorage } from '../storage/browser-storage';
import { isSession, Session } from './session.model';

/** Persists the session between reloads. Corrupt or foreign values are discarded silently. */
@Service()
export class TokenStorage {
  private static readonly KEY = 'rm.session';
  private readonly storage = inject(BrowserStorage);

  read(): Session | null {
    const raw = this.storage.get(TokenStorage.KEY);
    if (raw === null) {
      return null;
    }
    try {
      const parsed: unknown = JSON.parse(raw);
      return isSession(parsed) ? parsed : null;
    } catch {
      this.clear();
      return null;
    }
  }

  write(session: Session): void {
    this.storage.set(TokenStorage.KEY, JSON.stringify(session));
  }

  clear(): void {
    this.storage.remove(TokenStorage.KEY);
  }
}

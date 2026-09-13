import { Service } from '@angular/core';

/**
 * Single access point to `localStorage`. Every read/write is guarded: private mode, quota
 * exceeded or a blocked storage must never break the application.
 */
@Service()
export class BrowserStorage {
  get(key: string): string | null {
    try {
      return globalThis.localStorage?.getItem(key) ?? null;
    } catch {
      return null;
    }
  }

  set(key: string, value: string): void {
    try {
      globalThis.localStorage?.setItem(key, value);
    } catch {
      // Storage unavailable or full: the app keeps working with in-memory state only.
    }
  }

  remove(key: string): void {
    try {
      globalThis.localStorage?.removeItem(key);
    } catch {
      // Nothing to clean up.
    }
  }
}

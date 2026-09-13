import { BrowserStorage } from '../app/core/storage/browser-storage';

/** Replaces `BrowserStorage` so tests never touch the real `localStorage`. */
export class InMemoryStorage extends BrowserStorage {
  private readonly map = new Map<string, string>();

  override get(key: string): string | null {
    return this.map.get(key) ?? null;
  }

  override set(key: string, value: string): void {
    this.map.set(key, value);
  }

  override remove(key: string): void {
    this.map.delete(key);
  }
}

import { computed, DOCUMENT, effect, inject, Service, signal } from '@angular/core';
import { BrowserStorage } from '../storage/browser-storage';

export type Theme = 'light' | 'dark';

/**
 * Light/dark mode on top of Bootstrap 5.3 `data-bs-theme`. The choice is persisted; without a
 * choice the OS preference wins. `index.html` applies the same rule before Angular boots.
 */
@Service()
export class ThemeService {
  private static readonly KEY = 'rm.theme';
  private readonly storage = inject(BrowserStorage);
  private readonly document = inject(DOCUMENT);
  private readonly themeSignal = signal<Theme>(this.initialTheme());

  readonly theme = this.themeSignal.asReadonly();
  readonly isDark = computed(() => this.themeSignal() === 'dark');

  constructor() {
    effect(() => {
      const theme = this.themeSignal();
      this.document.documentElement.setAttribute('data-bs-theme', theme);
      this.document.documentElement.style.colorScheme = theme;
    });
  }

  toggle(): void {
    this.set(this.isDark() ? 'light' : 'dark');
  }

  set(theme: Theme): void {
    this.themeSignal.set(theme);
    this.storage.set(ThemeService.KEY, theme);
  }

  private initialTheme(): Theme {
    const stored = this.storage.get(ThemeService.KEY);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
    const prefersDark =
      typeof globalThis.matchMedia === 'function' &&
      globalThis.matchMedia('(prefers-color-scheme: dark)').matches;
    return prefersDark ? 'dark' : 'light';
  }
}

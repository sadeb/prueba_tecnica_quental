import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <button
      type="button"
      class="btn btn-outline-secondary btn-sm theme-toggle d-inline-flex align-items-center gap-2"
      (click)="toggle.emit()"
      [attr.aria-label]="label()"
      [title]="label()"
      [attr.aria-pressed]="dark()"
    >
      @if (dark()) {
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        </svg>
      } @else {
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
        </svg>
      }
      <span class="d-none d-sm-inline">{{ dark() ? 'Claro' : 'Oscuro' }}</span>
    </button>
  `,
})
export class ThemeToggleComponent {
  readonly dark = input(false);
  readonly toggle = output<void>();

  protected readonly label = computed(() =>
    this.dark() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro',
  );
}

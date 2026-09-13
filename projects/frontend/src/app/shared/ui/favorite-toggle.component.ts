import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-favorite-toggle',
  template: `
    <button
      type="button"
      class="btn favorite-toggle"
      [class.is-active]="active()"
      [class.btn-sm]="size() === 'sm'"
      [disabled]="disabled()"
      [attr.aria-pressed]="active()"
      [attr.aria-label]="ariaLabel()"
      [title]="ariaLabel()"
      (click)="toggle.emit()"
    >
      @if (disabled()) {
        <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
      } @else {
        <svg width="20" height="20" viewBox="0 0 24 24" [attr.fill]="active() ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8z" />
        </svg>
      }
      @if (showLabel()) {
        <span class="ms-2">{{ active() ? 'En favoritos' : 'Añadir a favoritos' }}</span>
      }
    </button>
  `,
})
export class FavoriteToggleComponent {
  readonly active = input(false);
  readonly disabled = input(false);
  readonly showLabel = input(false);
  readonly size = input<'sm' | 'md'>('sm');
  readonly toggle = output<void>();

  protected readonly ariaLabel = computed(() =>
    this.active() ? 'Quitar de favoritos' : 'Añadir a favoritos',
  );
}

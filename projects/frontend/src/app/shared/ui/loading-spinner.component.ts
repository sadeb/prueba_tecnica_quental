import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="d-flex flex-column align-items-center justify-content-center gap-3 py-5" role="status" aria-live="polite">
      <div class="spinner-border text-primary" aria-hidden="true"></div>
      <span class="text-body-secondary">{{ label() }}</span>
    </div>
  `,
})
export class LoadingSpinnerComponent {
  readonly label = input('Cargando…');
}

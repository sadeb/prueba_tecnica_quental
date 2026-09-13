import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  template: `
    <div class="empty-state text-center py-5 px-3">
      <div class="empty-state-icon mx-auto mb-3" aria-hidden="true">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="9" />
          <path d="M8 15s1.5-2 4-2 4 2 4 2" />
          <path d="M9 9h.01M15 9h.01" />
        </svg>
      </div>
      <h2 class="h5 mb-2">{{ title() }}</h2>
      @if (message()) {
        <p class="text-body-secondary mb-3 mx-auto empty-state-text">{{ message() }}</p>
      }
      <ng-content />
    </div>
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input('');
}

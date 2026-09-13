import { Component } from '@angular/core';

/** Loading placeholder with the same footprint as a character card (no layout jump). */
@Component({
  selector: 'app-skeleton-card',
  host: { class: 'd-block h-100', 'aria-hidden': 'true' },
  template: `
    <div class="card h-100 border-0 shadow-sm placeholder-glow">
      <div class="ratio ratio-1x1 placeholder rounded-top"></div>
      <div class="card-body">
        <span class="placeholder col-8 mb-2"></span>
        <span class="placeholder col-5 mb-2"></span>
        <span class="placeholder col-6"></span>
      </div>
    </div>
  `,
})
export class SkeletonCardComponent {}

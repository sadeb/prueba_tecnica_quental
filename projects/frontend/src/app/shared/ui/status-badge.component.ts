import { Component, computed, input } from '@angular/core';
import { statusLabel } from '../i18n/labels';
import { CharacterStatus } from '../models/character.model';

const CLASS_BY_STATUS: Readonly<Record<CharacterStatus, string>> = {
  ALIVE: 'status-alive',
  DEAD: 'status-dead',
  UNKNOWN: 'status-unknown',
};

@Component({
  selector: 'app-status-badge',
  template: `
    <span class="status-badge d-inline-flex align-items-center gap-2" [class]="cssClass()">
      <span class="status-dot" aria-hidden="true"></span>
      <span>{{ label() }}</span>
    </span>
  `,
})
export class StatusBadgeComponent {
  readonly status = input.required<CharacterStatus>();

  protected readonly label = computed(() => statusLabel(this.status()));
  protected readonly cssClass = computed(() => CLASS_BY_STATUS[this.status()] ?? CLASS_BY_STATUS.UNKNOWN);
}

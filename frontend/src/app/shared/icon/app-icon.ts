import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type IconName =
  | 'plus'
  | 'pencil'
  | 'trash'
  | 'search'
  | 'close'
  | 'check-circle'
  | 'alert-circle'
  | 'info-circle'
  | 'chevron-left'
  | 'chevron-right'
  | 'sort'
  | 'sort-asc'
  | 'sort-desc'
  | 'refresh'
  | 'user-plus';

const ICON_PATHS: Record<IconName, string> = {
  plus: 'M12 5v14M5 12h14',
  pencil: 'M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z',
  trash: 'M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6',
  search: 'M11 4a7 7 0 1 1 0 14 7 7 0 0 1 0-14ZM20 20l-4.3-4.3',
  close: 'M18 6 6 18M6 6l12 12',
  'check-circle': 'M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18ZM8.5 12.5l2.5 2.5 4.5-5',
  'alert-circle': 'M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18ZM12 8v5M12 16h.01',
  'info-circle': 'M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18ZM12 11v5M12 8h.01',
  'chevron-left': 'm15 6-6 6 6 6',
  'chevron-right': 'm9 6 6 6-6 6',
  sort: 'M8 4v16M8 4 5 7M8 4l3 3M16 20V4M16 20l-3-3M16 20l3-3',
  'sort-asc': 'M12 19V5M12 5 6 11M12 5l6 6',
  'sort-desc': 'M12 5v14M12 19l-6-6M12 19l6-6',
  refresh: 'M20 12a8 8 0 1 1-2.3-5.7M20 4v5h-5',
  'user-plus': 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8ZM19 8v6M22 11h-6',
};

@Component({
  selector: 'app-icon',
  template: `<svg
    [attr.width]="size()"
    [attr.height]="size()"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <path [attr.d]="path()" />
  </svg>`,
  styles: `
    :host {
      display: inline-flex;
      flex: 0 0 auto;
      line-height: 0;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppIcon {
  readonly name = input.required<IconName>();
  readonly size = input(20);

  protected path(): string {
    return ICON_PATHS[this.name()];
  }
}

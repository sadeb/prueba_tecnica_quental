import { Component, computed, input, output } from '@angular/core';

const WINDOW = 5;

@Component({
  selector: 'app-pagination',
  host: { class: 'd-block' },
  template: `
    @if (totalPages() > 1) {
      <nav [attr.aria-label]="label()" class="d-flex flex-column flex-sm-row align-items-center justify-content-between gap-2">
        <p class="text-body-secondary small mb-0">
          Página {{ page() + 1 }} de {{ totalPages() }}
        </p>
        <ul class="pagination mb-0">
          <li class="page-item" [class.disabled]="isFirst()">
            <button type="button" class="page-link" (click)="go(page() - 1)" [disabled]="isFirst()" aria-label="Página anterior">
              <span aria-hidden="true">‹</span>
            </button>
          </li>
          @for (item of pages(); track item) {
            <li class="page-item" [class.active]="item === page()">
              <button
                type="button"
                class="page-link"
                (click)="go(item)"
                [attr.aria-current]="item === page() ? 'page' : null"
                [attr.aria-label]="'Página ' + (item + 1)"
              >
                {{ item + 1 }}
              </button>
            </li>
          }
          <li class="page-item" [class.disabled]="isLast()">
            <button type="button" class="page-link" (click)="go(page() + 1)" [disabled]="isLast()" aria-label="Página siguiente">
              <span aria-hidden="true">›</span>
            </button>
          </li>
        </ul>
      </nav>
    }
  `,
})
export class PaginationComponent {
  /** 0-based, like the API. */
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly label = input('Paginación');
  readonly pageChange = output<number>();

  protected readonly isFirst = computed(() => this.page() <= 0);
  protected readonly isLast = computed(() => this.page() >= this.totalPages() - 1);

  protected readonly pages = computed(() => {
    const total = this.totalPages();
    const half = Math.floor(WINDOW / 2);
    let start = Math.max(0, this.page() - half);
    const end = Math.min(total, start + WINDOW);
    start = Math.max(0, end - WINDOW);
    return Array.from({ length: end - start }, (_, index) => start + index);
  });

  protected go(target: number): void {
    if (target >= 0 && target < this.totalPages() && target !== this.page()) {
      this.pageChange.emit(target);
    }
  }
}

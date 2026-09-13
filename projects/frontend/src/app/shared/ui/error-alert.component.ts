import { afterNextRender, Component, computed, ElementRef, inject, input, output } from '@angular/core';
import { ApiError } from '../../core/errors/api-error.model';
import { describeError, ErrorContext, fieldErrors } from '../../core/errors/error-messages';

/**
 * The single way an `ApiError` is shown. Announces itself to screen readers, receives focus so
 * keyboard users land on it, and offers "retry" when the caller listens to the output.
 */
@Component({
  selector: 'app-error-alert',
  host: { tabindex: '-1', class: 'd-block' },
  template: `
    <div class="alert alert-danger d-flex gap-3 align-items-start mb-0" role="alert" aria-live="assertive">
      <svg class="flex-shrink-0 mt-1" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
        <path d="M12 9v4M12 17h.01" />
      </svg>
      <div class="flex-grow-1">
        <p class="fw-semibold mb-1">{{ title() }}</p>
        <p class="mb-0">{{ message() }}</p>
        @if (details().length > 0) {
          <ul class="mb-0 mt-2 ps-3">
            @for (detail of details(); track detail) {
              <li>{{ detail }}</li>
            }
          </ul>
        }
        @if (technical()) {
          <p class="small text-body-secondary mb-0 mt-2">
            <span class="visually-hidden">Detalle técnico: </span>{{ technical() }}
          </p>
        }
        @if (canRetry()) {
          <button type="button" class="btn btn-outline-danger btn-sm mt-3" (click)="retry.emit()">
            {{ retryLabel() }}
          </button>
        }
      </div>
    </div>
  `,
})
export class ErrorAlertComponent {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly error = input.required<ApiError>();
  readonly context = input<ErrorContext>('generic');
  readonly title = input('No ha sido posible completar la operación');
  readonly retryLabel = input('Reintentar');
  /** Show the retry button even for errors that are not transient (e.g. reload a whole list). */
  readonly alwaysRetry = input(false);
  readonly retry = output<void>();

  protected readonly message = computed(() => describeError(this.error(), this.context()));
  protected readonly details = computed(() => fieldErrors(this.error()));
  protected readonly technical = computed(() => {
    const error = this.error();
    return error.status > 0 ? `HTTP ${error.status} · ${error.code}` : error.code;
  });
  protected readonly canRetry = computed(() => this.alwaysRetry() || this.error().retryable);

  constructor() {
    afterNextRender(() => this.host.nativeElement.focus({ preventScroll: false }));
  }
}

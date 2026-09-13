import { ErrorHandler, inject, Service } from '@angular/core';
import { ToastService } from '../notifications/toast.service';
import { isApiError } from './api-error.model';

/**
 * Last line of defence: anything nobody caught ends in a toast instead of a silent console line.
 * `ApiError`s that escape a page are shown with their Spanish message.
 */
@Service({ autoProvided: false })
export class GlobalErrorHandler implements ErrorHandler {
  private readonly toasts = inject(ToastService);

  handleError(error: unknown): void {
    console.error(error);
    const message = isApiError(error)
      ? error.userMessage
      : 'Algo ha salido mal en la interfaz. Recarga la página si el problema continúa.';
    this.toasts.error(message);
  }
}

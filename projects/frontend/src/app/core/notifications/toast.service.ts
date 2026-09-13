import { Service, signal } from '@angular/core';

export type ToastKind = 'success' | 'danger' | 'warning' | 'info';

export interface Toast {
  readonly id: number;
  readonly kind: ToastKind;
  readonly message: string;
}

const DEFAULT_DURATION_MS = 4500;

/** Ephemeral notifications for actions that do not own a screen region (favorites, unexpected errors). */
@Service()
export class ToastService {
  private readonly toastsSignal = signal<readonly Toast[]>([]);
  private nextId = 1;

  readonly toasts = this.toastsSignal.asReadonly();

  show(message: string, kind: ToastKind = 'info', durationMs: number = DEFAULT_DURATION_MS): void {
    const toast: Toast = { id: this.nextId++, kind, message };
    this.toastsSignal.update((toasts) => [...toasts, toast]);
    if (durationMs > 0) {
      setTimeout(() => this.dismiss(toast.id), durationMs);
    }
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'danger', 7000);
  }

  warning(message: string): void {
    this.show(message, 'warning', 6000);
  }

  dismiss(id: number): void {
    this.toastsSignal.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }
}

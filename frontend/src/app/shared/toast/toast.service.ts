import { Injectable, signal } from '@angular/core';

export type ToastTone = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  tone: ToastTone;
  title: string;
  message: string;
}

const DEFAULT_DURATION_MS = 5000;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly items = signal<Toast[]>([]);

  readonly toasts = this.items.asReadonly();

  success(message: string, title = 'Listo'): void {
    this.show('success', title, message);
  }

  error(message: string, title = 'No fue posible'): void {
    this.show('error', title, message, 8000);
  }

  info(message: string, title = 'Aviso'): void {
    this.show('info', title, message);
  }

  show(tone: ToastTone, title: string, message: string, durationMs = DEFAULT_DURATION_MS): void {
    const toast: Toast = { id: this.nextId++, tone, title, message };
    this.items.update((current) => [...current, toast]);
    if (durationMs > 0) {
      this.timers.set(
        toast.id,
        setTimeout(() => this.dismiss(toast.id), durationMs),
      );
    }
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this.items.update((current) => current.filter((toast) => toast.id !== id));
  }
}

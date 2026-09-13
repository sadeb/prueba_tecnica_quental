import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/notifications/toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div class="toast-container position-fixed bottom-0 end-0 p-3" aria-live="polite" aria-atomic="false">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="toast show align-items-center border-0 mb-2" [class]="'text-bg-' + toast.kind" role="status">
          <div class="d-flex">
            <div class="toast-body">{{ toast.message }}</div>
            <button
              type="button"
              class="btn-close me-2 m-auto"
              [class.btn-close-white]="toast.kind !== 'warning'"
              aria-label="Cerrar aviso"
              (click)="toasts.dismiss(toast.id)"
            ></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  protected readonly toasts = inject(ToastService);
}

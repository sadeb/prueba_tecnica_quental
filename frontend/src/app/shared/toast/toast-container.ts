import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AppIcon } from '../icon/app-icon';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  imports: [AppIcon],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastContainer {
  protected readonly toastService = inject(ToastService);

  protected iconFor(tone: 'success' | 'error' | 'info'): 'check-circle' | 'alert-circle' | 'info-circle' {
    if (tone === 'success') return 'check-circle';
    if (tone === 'error') return 'alert-circle';
    return 'info-circle';
  }
}

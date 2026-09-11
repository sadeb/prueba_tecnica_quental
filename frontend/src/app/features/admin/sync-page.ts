import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SyncApi } from '../../core/api/sync-api';
import { apiErrorMessage } from '../../core/http/api-error-message';
import { SyncRun } from '../../core/models/sync.models';

@Component({
  selector: 'app-sync-page',
  imports: [DatePipe],
  templateUrl: './sync-page.html',
  styleUrl: './sync-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SyncPage {
  private readonly api = inject(SyncApi);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly runs = signal<SyncRun[]>([]);
  protected readonly loading = signal(true);
  protected readonly starting = signal(false);
  protected readonly errorMessage = signal('');

  constructor() { this.load(); }

  protected load(): void {
    this.loading.set(true); this.errorMessage.set('');
    this.api.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => { this.runs.set(page.content); this.loading.set(false); },
      error: (error) => { this.errorMessage.set(apiErrorMessage(error, 'No fue posible consultar las sincronizaciones.')); this.loading.set(false); },
    });
  }

  protected start(): void {
    if (this.starting()) return;
    this.starting.set(true); this.errorMessage.set('');
    this.api.start().subscribe({
      next: (run) => { this.runs.update((runs) => [run, ...runs]); this.starting.set(false); },
      error: (error) => { this.errorMessage.set(apiErrorMessage(error, 'No fue posible iniciar la sincronización.')); this.starting.set(false); },
    });
  }

  protected progress(run: SyncRun): number {
    if (!run.messagesQueued) return run.status === 'COMPLETED' ? 100 : 0;
    return Math.min(100, Math.round(((run.processedCount + run.failedCount) / run.messagesQueued) * 100));
  }
}

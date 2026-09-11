import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subscription, catchError, exhaustMap, finalize, takeWhile, timer } from 'rxjs';
import { SyncApi } from '../../core/api/sync-api';
import { apiErrorMessage } from '../../core/http/api-error-message';
import { SyncRun } from '../../core/models/sync.models';
import {
  isTerminalSyncRun,
  syncErrorMessage,
  syncStatusLabel,
  syncTriggerLabel,
} from './sync-presentation';

const POLL_INTERVAL_MS = 60_000;

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
  protected readonly pollingRunId = signal<string | null>(null);
  protected readonly hasRuns = computed(() => this.runs().length > 0);
  protected readonly polling = computed(() => this.pollingRunId() !== null);
  protected readonly statusLabel = syncStatusLabel;
  protected readonly triggerLabel = syncTriggerLabel;
  protected readonly runErrorMessage = syncErrorMessage;
  private pollingSubscription: Subscription | null = null;

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
    this.api.start().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (run) => {
        this.upsertRun(run);
        this.starting.set(false);
        this.startPolling(run.id);
      },
      error: (error) => { this.errorMessage.set(apiErrorMessage(error, 'No fue posible iniciar la sincronización.')); this.starting.set(false); },
    });
  }

  private startPolling(runId: string): void {
    this.pollingSubscription?.unsubscribe();
    this.pollingRunId.set(runId);
    this.pollingSubscription = timer(POLL_INTERVAL_MS, POLL_INTERVAL_MS)
      .pipe(
        exhaustMap(() => this.api.get(runId).pipe(catchError(() => EMPTY))),
        takeWhile((run) => !isTerminalSyncRun(run), true),
        finalize(() => this.pollingRunId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((run) => this.upsertRun(run));
  }

  private upsertRun(run: SyncRun): void {
    this.runs.update((runs) => [run, ...runs.filter((current) => current.id !== run.id)]);
  }

  protected progress(run: SyncRun): number {
    if (!run.messagesQueued) return run.status === 'COMPLETED' ? 100 : 0;
    return Math.min(100, Math.round(((run.processedCount + run.failedCount) / run.messagesQueued) * 100));
  }
}

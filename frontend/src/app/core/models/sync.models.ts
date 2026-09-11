export type SyncStatus =
  | 'QUEUING'
  | 'QUEUED'
  | 'COMPLETED'
  | 'COMPLETED_WITH_ERRORS'
  | 'FAILED';

export type SyncTrigger = 'MANUAL' | 'AUTOMATIC';

export interface SyncRun {
  id: string;
  status: SyncStatus;
  trigger: SyncTrigger;
  startedAt: string;
  completedAt: string | null;
  pagesFetched: number;
  messagesQueued: number;
  processedCount: number;
  failedCount: number;
  errorMessage: string | null;
}

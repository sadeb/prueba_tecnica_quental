export type SyncStatus =
  | 'QUEUING'
  | 'QUEUED'
  | 'COMPLETED'
  | 'COMPLETED_WITH_ERRORS'
  | 'FAILED';

export interface SyncRun {
  id: string;
  status: SyncStatus;
  startedAt: string;
  completedAt: string | null;
  pagesFetched: number;
  messagesQueued: number;
  processedCount: number;
  failedCount: number;
  errorMessage: string | null;
}

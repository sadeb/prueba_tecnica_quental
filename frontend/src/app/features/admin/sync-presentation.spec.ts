import { SyncRun } from '../../core/models/sync.models';
import {
  isTerminalSyncRun,
  syncErrorMessage,
  syncStatusLabel,
  syncTriggerLabel,
} from './sync-presentation';

function run(status: SyncRun['status']): SyncRun {
  return {
    id: 'run-1',
    status,
    trigger: 'MANUAL',
    startedAt: '2026-09-11T19:18:46Z',
    completedAt: null,
    pagesFetched: 25,
    messagesQueued: 637,
    processedCount: 637,
    failedCount: 0,
    errorMessage: null,
  };
}

describe('sync presentation', () => {
  it('translates status and trigger codes for the interface', () => {
    expect(syncStatusLabel('FAILED')).toBe('Fallida');
    expect(syncTriggerLabel('AUTOMATIC')).toBe('Automática');
  });

  it('explains the external character page failure in Spanish', () => {
    expect(syncErrorMessage('Unable to fetch character page 24')).toBe(
      'No se pudo descargar la página 24 de personajes desde el origen externo.',
    );
  });

  it('distinguishes active runs from terminal runs', () => {
    expect(isTerminalSyncRun(run('QUEUED'))).toBeFalse();
    expect(isTerminalSyncRun(run('FAILED'))).toBeTrue();
  });
});

import { SyncRun, SyncStatus, SyncTrigger } from '../../core/models/sync.models';

const STATUS_LABELS: Record<SyncStatus, string> = {
  QUEUING: 'Descargando',
  QUEUED: 'Procesando',
  COMPLETED: 'Completada',
  COMPLETED_WITH_ERRORS: 'Completada con errores',
  FAILED: 'Fallida',
};

const TRIGGER_LABELS: Record<SyncTrigger, string> = {
  MANUAL: 'Manual',
  AUTOMATIC: 'Automática',
};

const RESOURCE_LABELS: Record<string, string> = {
  character: 'personajes',
  episode: 'episodios',
  location: 'ubicaciones',
};

export function syncStatusLabel(status: SyncStatus): string {
  return STATUS_LABELS[status];
}

export function syncTriggerLabel(trigger: SyncTrigger): string {
  return TRIGGER_LABELS[trigger];
}

export function syncErrorMessage(message: string | null): string | null {
  if (!message) return null;

  const fetchFailure = /^Unable to fetch (character|episode|location) page (\d+)$/.exec(message);
  if (fetchFailure) {
    return `No se pudo descargar la página ${fetchFailure[2]} de ${RESOURCE_LABELS[fetchFailure[1]]} desde el origen externo.`;
  }

  return message;
}

export function isTerminalSyncRun(run: SyncRun): boolean {
  return ['COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED'].includes(run.status);
}

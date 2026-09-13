import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api.models';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as Partial<ApiError> | null;
    if (typeof body?.code === 'string' && typeof body.message === 'string') return body.message;
    if (error.status === 0) return 'No se pudo conectar con la API propia.';
  }
  return fallback;
}

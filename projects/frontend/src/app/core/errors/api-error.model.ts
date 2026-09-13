export interface ApiFieldError {
  readonly field: string;
  readonly message: string;
}

/** Body produced by the backend `GlobalExceptionHandler` (conventions/formato-error.md). */
export interface ApiErrorBody {
  readonly timestamp?: string;
  readonly status: number;
  readonly error: string;
  readonly message?: string;
  readonly path?: string;
  readonly details?: readonly ApiFieldError[];
}

export type ApiErrorCode =
  | 'VALIDATION_ERROR'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'METHOD_NOT_ALLOWED'
  | 'CONFLICT'
  | 'EXTERNAL_SERVICE_ERROR'
  | 'SERVICE_UNAVAILABLE'
  | 'INTERNAL_ERROR'
  /** Synthesised by the frontend: no HTTP response at all (server down, DNS, CORS, offline). */
  | 'NETWORK_ERROR'
  /** Synthesised by the frontend: the request exceeded `environment.requestTimeoutMs`. */
  | 'TIMEOUT'
  | 'UNKNOWN_ERROR';

export interface ApiErrorInit {
  readonly status: number;
  readonly code: ApiErrorCode | string;
  readonly userMessage: string;
  readonly serverMessage?: string;
  readonly path?: string;
  readonly details?: readonly ApiFieldError[];
}

/** Every HTTP failure reaches pages as an `ApiError`; `userMessage` is already in Spanish. */
export class ApiError extends Error {
  override readonly name = 'ApiError';
  readonly status: number;
  readonly code: string;
  readonly userMessage: string;
  readonly serverMessage: string | undefined;
  readonly path: string | undefined;
  readonly details: readonly ApiFieldError[];

  constructor(init: ApiErrorInit) {
    super(init.serverMessage ?? init.userMessage);
    this.status = init.status;
    this.code = init.code;
    this.userMessage = init.userMessage;
    this.serverMessage = init.serverMessage;
    this.path = init.path;
    this.details = init.details ?? [];
  }

  /** The request itself was fine; the failure is on the wire or the server: worth retrying. */
  get transient(): boolean {
    return this.status === 0 || this.status === 502 || this.status === 503 || this.status === 504;
  }

  /** Whether offering a "retry" action makes sense to the user. */
  get retryable(): boolean {
    return this.transient || this.status === 408 || this.status >= 500;
  }

  get isValidation(): boolean {
    return this.status === 400;
  }

  fieldMessage(field: string): string | undefined {
    return this.details.find((detail) => detail.field === field)?.message;
  }
}

export function isApiError(value: unknown): value is ApiError {
  return value instanceof ApiError;
}

export function isApiErrorBody(value: unknown): value is ApiErrorBody {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return typeof candidate['status'] === 'number' && typeof candidate['error'] === 'string';
}

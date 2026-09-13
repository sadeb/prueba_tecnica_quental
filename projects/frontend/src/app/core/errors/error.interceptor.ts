import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError, timeout } from 'rxjs';
import { environment } from '../../../environments/environment';
import { isAuthEndpoint } from '../auth/auth.interceptor';
import { AuthService } from '../auth/auth.service';
import { ApiError, ApiErrorCode, isApiError, isApiErrorBody } from './api-error.model';
import { messageFor } from './error-messages';

const CODE_BY_STATUS: Readonly<Record<number, ApiErrorCode>> = {
  400: 'VALIDATION_ERROR',
  401: 'UNAUTHORIZED',
  403: 'FORBIDDEN',
  404: 'NOT_FOUND',
  405: 'METHOD_NOT_ALLOWED',
  408: 'TIMEOUT',
  409: 'CONFLICT',
  500: 'INTERNAL_ERROR',
  502: 'EXTERNAL_SERVICE_ERROR',
  503: 'SERVICE_UNAVAILABLE',
  504: 'TIMEOUT',
};

/**
 * Turns any HTTP failure into an `ApiError` with a Spanish message, enforces a request timeout
 * and reacts centrally to 401 (expired or forged token): the session is dropped and the user is
 * taken back to `/login` keeping the URL they were on. A 401 from the login endpoint itself is a
 * wrong-credentials error and is left to the login page.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    timeout({
      each: environment.requestTimeoutMs,
      with: () => throwError(() => timeoutError(req)),
    }),
    catchError((error: unknown) => {
      const apiError = isApiError(error) ? error : toApiError(error, req);
      if (apiError.status === 401 && !isAuthEndpoint(req.url)) {
        auth.logout();
        void router.navigate(['/login'], {
          queryParams: { returnUrl: router.url, reason: 'expired' },
        });
      }
      return throwError(() => apiError);
    }),
  );
};

export function toApiError(error: unknown, req: HttpRequest<unknown>): ApiError {
  if (!(error instanceof HttpErrorResponse)) {
    return new ApiError({
      status: 0,
      code: 'UNKNOWN_ERROR',
      userMessage: messageFor(-1, 'UNKNOWN_ERROR'),
      serverMessage: error instanceof Error ? error.message : undefined,
      path: req.url,
    });
  }
  if (error.status === 0) {
    return new ApiError({
      status: 0,
      code: 'NETWORK_ERROR',
      userMessage: messageFor(0, 'NETWORK_ERROR'),
      serverMessage: error.message,
      path: req.url,
    });
  }
  const body: unknown = error.error;
  if (isApiErrorBody(body)) {
    return new ApiError({
      status: body.status,
      code: body.error,
      userMessage: messageFor(body.status, body.error),
      serverMessage: body.message,
      path: body.path ?? req.url,
      details: body.details,
    });
  }
  // Non-JSON body (nginx 502 page, empty body, proxy error): classify by status only.
  const code = CODE_BY_STATUS[error.status] ?? 'UNKNOWN_ERROR';
  return new ApiError({
    status: error.status,
    code,
    userMessage: messageFor(error.status, code),
    serverMessage: error.statusText || undefined,
    path: req.url,
  });
}

function timeoutError(req: HttpRequest<unknown>): ApiError {
  return new ApiError({
    status: 408,
    code: 'TIMEOUT',
    userMessage: messageFor(408, 'TIMEOUT'),
    serverMessage: `Timeout after ${environment.requestTimeoutMs} ms`,
    path: req.url,
  });
}

import { HttpInterceptorFn } from '@angular/common/http';
import { retry, throwError, timer } from 'rxjs';
import { environment } from '../../../environments/environment';
import { isApiError } from './api-error.model';

/**
 * Idempotent reads get one automatic retry on transient failures (network, 502/503/504), which
 * covers the backend restarting behind nginx. Runs before `errorInterceptor` in the pipeline so
 * the error it sees is already an `ApiError`.
 */
export const retryInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.method !== 'GET') {
    return next(req);
  }
  return next(req).pipe(
    retry({
      count: 1,
      delay: (error: unknown) =>
        isApiError(error) && error.transient
          ? timer(environment.transientRetryDelayMs)
          : throwError(() => error),
    }),
  );
};

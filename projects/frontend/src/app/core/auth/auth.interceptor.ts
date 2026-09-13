import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export function isApiUrl(url: string): boolean {
  return url.startsWith(environment.apiBaseUrl);
}

export function isAuthEndpoint(url: string): boolean {
  return url.startsWith(`${environment.apiBaseUrl}/auth/`);
}

/** Adds the bearer token to every request against our API (login/register carry no token). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();
  if (token === null || !isApiUrl(req.url) || isAuthEndpoint(req.url)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};

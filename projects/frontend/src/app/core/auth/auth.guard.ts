import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Every route except `/login` requires a live session; the requested URL is kept for after login. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const status = auth.sessionStatus();
  if (status === 'valid') {
    return true;
  }
  return router.createUrlTree(['/login'], {
    queryParams: {
      returnUrl: state.url,
      ...(status === 'expired' ? { reason: 'expired' } : {}),
    },
  });
};

export const authChildGuard: CanActivateChildFn = (route, state) => authGuard(route, state);

/** `/login` is only for visitors: an authenticated user is sent to the characters list. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasValidSession() ? router.createUrlTree(['/characters']) : true;
};

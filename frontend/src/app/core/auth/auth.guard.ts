import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth-store';

export const authGuard: CanActivateFn = (_route, state) => {
  const authStore = inject(AuthStore);
  return authStore.isAuthenticated()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const adminGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  return authStore.isAdmin() ? true : inject(Router).createUrlTree(['/characters']);
};

import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';
import { AuthService, SessionStatus } from './auth.service';

describe('authGuard / guestGuard', () => {
  let status: SessionStatus;
  let router: Router;

  const fakeAuth = {
    sessionStatus: () => status,
    hasValidSession: () => status === 'valid',
  };

  beforeEach(() => {
    status = 'none';
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: fakeAuth }],
    });
    router = TestBed.inject(Router);
  });

  function runAuthGuard(url: string): boolean | UrlTree {
    return TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    ) as boolean | UrlTree;
  }

  it('allows navigation with a valid session', () => {
    status = 'valid';
    expect(runAuthGuard('/favorites')).toBe(true);
  });

  it('redirects to /login keeping the requested url when there is no session', () => {
    const result = runAuthGuard('/characters/42?tab=episodes');
    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login?returnUrl=%2Fcharacters%2F42%3Ftab%3Depisodes');
  });

  it('adds reason=expired when the session has just expired', () => {
    status = 'expired';
    const result = runAuthGuard('/favorites') as UrlTree;
    expect(result.queryParams).toEqual({ returnUrl: '/favorites', reason: 'expired' });
  });

  it('guestGuard lets visitors into /login and sends signed-in users to /characters', () => {
    const run = () =>
      TestBed.runInInjectionContext(() =>
        guestGuard({} as ActivatedRouteSnapshot, { url: '/login' } as RouterStateSnapshot),
      );
    expect(run()).toBe(true);
    status = 'valid';
    expect(router.serializeUrl(run() as UrlTree)).toBe('/characters');
  });
});

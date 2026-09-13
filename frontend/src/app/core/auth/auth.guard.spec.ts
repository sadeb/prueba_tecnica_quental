import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../app.routes';
import { adminGuard, authGuard } from './auth.guard';
import { AuthStore } from './auth-store';

@Component({ template: '' })
class RouteStub {}

describe('authentication route guards', () => {
  let authStore: AuthStore;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'login', component: RouteStub },
          {
            path: '',
            canActivateChild: [authGuard],
            children: [
              { path: 'characters', component: RouteStub },
              { path: 'admin/users', canActivate: [adminGuard], component: RouteStub },
            ],
          },
        ]),
      ],
    });
    authStore = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => authStore.clear());

  it('keeps login as the only public application route', () => {
    expect(routes).toHaveSize(2);
    expect(routes[0].path).toBe('login');
    expect(routes[0].canActivate).toBeUndefined();
    expect(routes[0].canActivateChild).toBeUndefined();
    expect(routes[1].path).toBe('');
    expect(routes[1].canActivateChild).toContain(authGuard);
  });

  it('redirects an unauthenticated user from a private screen to login', async () => {
    const harness = await RouterTestingHarness.create('/login');

    await harness.navigateByUrl('/characters');

    expect(router.url).toBe('/login?returnUrl=%2Fcharacters');
  });

  it('redirects a standard user away from an administrative screen', async () => {
    setSession('USER');
    const harness = await RouterTestingHarness.create('/login');

    await harness.navigateByUrl('/admin/users');

    expect(router.url).toBe('/characters');
  });

  it('allows an administrator to open the user management screen', async () => {
    setSession('ADMIN');
    const harness = await RouterTestingHarness.create('/login');

    await harness.navigateByUrl('/admin/users', RouteStub);

    expect(router.url).toBe('/admin/users');
  });

  function setSession(role: 'USER' | 'ADMIN'): void {
    authStore.setSession({
      token: 'opaque-token-value',
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      user: { id: 7, username: 'operator', role },
    });
  }
});

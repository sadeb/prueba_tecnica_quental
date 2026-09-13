import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, ParamMap, provideRouter, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { InMemoryStorage } from '../../../testing/in-memory-storage';
import { anApiErrorBody, isoInMinutes } from '../../../testing/test-data';
import { AuthService } from '../../core/auth/auth.service';
import { errorInterceptor } from '../../core/errors/error.interceptor';
import { BrowserStorage } from '../../core/storage/browser-storage';
import { LoginPage } from './login.page';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let controller: HttpTestingController;
  let router: Router;
  let queryParams: BehaviorSubject<ParamMap>;

  function element<T extends HTMLElement>(selector: string): T {
    const found = (fixture.nativeElement as HTMLElement).querySelector<T>(selector);
    if (found === null) {
      throw new Error(`Missing ${selector}`);
    }
    return found;
  }

  function type(selector: string, value: string): void {
    const input = element<HTMLInputElement>(selector);
    input.value = value;
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new Event('blur'));
  }

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  async function setup(params: Record<string, string> = {}): Promise<void> {
    queryParams = new BehaviorSubject<ParamMap>(convertToParamMap(params));
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: BrowserStorage, useValue: new InMemoryStorage() },
        {
          provide: ActivatedRoute,
          useValue: { queryParamMap: queryParams.asObservable(), snapshot: { queryParamMap: convertToParamMap(params) } },
        },
      ],
    }).compileComponents();
    controller = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(LoginPage);
    fixture.detectChanges();
  }

  afterEach(() => controller.verify());

  it('does not call the backend when the form is invalid and shows field messages', async () => {
    await setup();
    element<HTMLFormElement>('#panel-login').dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    fixture.detectChanges();
    controller.expectNone('/api/auth/login');
    expect(text()).toContain('Introduce tu nombre de usuario.');
    expect(text()).toContain('Introduce tu contraseña.');
  });

  it('shows "credenciales incorrectas" on 401 without redirecting', async () => {
    await setup();
    type('#login-username', 'rick');
    type('#login-password', 'wrong-password');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-login').dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    controller
      .expectOne('/api/auth/login')
      .flush(anApiErrorBody({ status: 401, error: 'UNAUTHORIZED', message: 'Bad credentials' }), {
        status: 401,
        statusText: 'Unauthorized',
      });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(text()).toContain('Usuario o contraseña incorrectos.');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(TestBed.inject(AuthService).isAuthenticated()).toBe(false);
  });

  it('logs in and returns to the requested url', async () => {
    await setup({ returnUrl: '/characters/7', reason: 'expired' });
    expect(text()).toContain('Tu sesión ha caducado');
    type('#login-username', 'rick');
    type('#login-password', 'wubbalubba');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-login').dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    controller.expectOne('/api/auth/login').flush({ token: 't', expiresAt: isoInMinutes(60), username: 'rick' });
    await fixture.whenStable();

    expect(TestBed.inject(AuthService).isAuthenticated()).toBe(true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/characters/7');
  });

  it('never redirects to an external or login url after signing in', async () => {
    await setup({ returnUrl: '//evil.example' });
    type('#login-username', 'rick');
    type('#login-password', 'wubbalubba');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-login').dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    controller.expectOne('/api/auth/login').flush({ token: 't', expiresAt: isoInMinutes(60), username: 'rick' });
    await fixture.whenStable();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/characters');
  });

  it('register validates locally (pattern, length, confirmation) before calling the backend', async () => {
    await setup({ mode: 'register' });
    expect(text()).toContain('Crea tu cuenta');
    type('#register-username', 'ri ck');
    type('#register-password', 'short');
    type('#register-confirm', 'other');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-register').dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    fixture.detectChanges();
    controller.expectNone('/api/auth/register');
    expect(text()).toContain('Solo letras, números');
    expect(text()).toContain('Mínimo 8 caracteres.');
    expect(text()).toContain('Las contraseñas no coinciden.');
  });

  it('register 409 shows the "usuario en uso" message', async () => {
    await setup({ mode: 'register' });
    type('#register-username', 'rick');
    type('#register-password', 'wubbalubba');
    type('#register-confirm', 'wubbalubba');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-register').dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    controller
      .expectOne('/api/auth/register')
      .flush(anApiErrorBody({ status: 409, error: 'CONFLICT', message: 'Username already exists' }), {
        status: 409,
        statusText: 'Conflict',
      });
    await fixture.whenStable();
    fixture.detectChanges();
    expect(text()).toContain('Ese nombre de usuario ya está en uso.');
    controller.expectNone('/api/auth/login');
  });

  it('register then logs in automatically', async () => {
    await setup({ mode: 'register' });
    type('#register-username', 'morty');
    type('#register-password', 'wubbalubba');
    type('#register-confirm', 'wubbalubba');
    fixture.detectChanges();
    element<HTMLFormElement>('#panel-register').dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    controller.expectOne('/api/auth/register').flush({ id: 2, username: 'morty' }, { status: 201, statusText: 'Created' });
    await fixture.whenStable();
    controller.expectOne('/api/auth/login').flush({ token: 't', expiresAt: isoInMinutes(60), username: 'morty' });
    await fixture.whenStable();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/characters');
  });
});

import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { anApiErrorBody } from '../../../testing/test-data';
import { AuthService } from '../auth/auth.service';
import { ApiError, isApiError } from './api-error.model';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let router: Router;
  const auth = { logout: vi.fn() };

  beforeEach(() => {
    auth.logout.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => controller.verify());

  function requestAndFail(url: string, flush: (req: ReturnType<HttpTestingController['expectOne']>) => void): ApiError {
    let captured: unknown;
    http.get(url).subscribe({ error: (error: unknown) => (captured = error) });
    flush(controller.expectOne(url));
    if (!isApiError(captured)) {
      throw new Error('Expected an ApiError');
    }
    return captured;
  }

  it('maps the backend error body to an ApiError with a Spanish message', () => {
    const error = requestAndFail('/api/characters/999', (req) =>
      req.flush(anApiErrorBody(), { status: 404, statusText: 'Not Found' }),
    );
    expect(error.status).toBe(404);
    expect(error.code).toBe('NOT_FOUND');
    expect(error.serverMessage).toBe('Character 999 not found');
    expect(error.userMessage).toBe('No hemos encontrado lo que buscas en esta dimensión.');
    expect(error.retryable).toBe(false);
  });

  it('keeps validation details from a 400 response', () => {
    const error = requestAndFail('/api/characters', (req) =>
      req.flush(
        anApiErrorBody({
          status: 400,
          error: 'VALIDATION_ERROR',
          message: 'Validation failed',
          details: [{ field: 'size', message: 'must be less than or equal to 100' }],
        }),
        { status: 400, statusText: 'Bad Request' },
      ),
    );
    expect(error.isValidation).toBe(true);
    expect(error.fieldMessage('size')).toBe('must be less than or equal to 100');
  });

  it('classifies a non-JSON body (proxy error page) by status', () => {
    const error = requestAndFail('/api/characters', (req) =>
      req.flush('<html>502 Bad Gateway</html>', { status: 502, statusText: 'Bad Gateway' }),
    );
    expect(error.code).toBe('EXTERNAL_SERVICE_ERROR');
    expect(error.transient).toBe(true);
  });

  it('turns a network failure into NETWORK_ERROR (status 0, retryable)', () => {
    const error = requestAndFail('/api/characters', (req) => req.error(new ProgressEvent('error'), { status: 0 }));
    expect(error.status).toBe(0);
    expect(error.code).toBe('NETWORK_ERROR');
    expect(error.transient).toBe(true);
    expect(error.userMessage).toContain('No hay conexión');
  });

  it('on 401 outside /auth it drops the session and goes to /login with returnUrl and reason', () => {
    requestAndFail('/api/users/me/favorites', (req) =>
      req.flush(anApiErrorBody({ status: 401, error: 'UNAUTHORIZED' }), { status: 401, statusText: 'Unauthorized' }),
    );
    expect(auth.logout).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: router.url, reason: 'expired' },
    });
  });

  it('a 401 from the login endpoint is a credentials error: no logout, no redirect', () => {
    let captured: unknown;
    http.post('/api/auth/login', {}).subscribe({ error: (error: unknown) => (captured = error) });
    controller
      .expectOne('/api/auth/login')
      .flush(anApiErrorBody({ status: 401, error: 'UNAUTHORIZED' }), { status: 401, statusText: 'Unauthorized' });
    expect(isApiError(captured) && captured.status).toBe(401);
    expect(auth.logout).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('fails with TIMEOUT when the server does not answer in time', () => {
    vi.useFakeTimers();
    try {
      let captured: unknown;
      http.get('/api/characters').subscribe({ error: (error: unknown) => (captured = error) });
      controller.expectOne('/api/characters');
      vi.advanceTimersByTime(15_001);
      expect(isApiError(captured) && captured.code).toBe('TIMEOUT');
      expect(isApiError(captured) && captured.status).toBe(408);
    } finally {
      vi.useRealTimers();
    }
  });
});

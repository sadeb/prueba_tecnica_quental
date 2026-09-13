import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { anApiErrorBody } from '../../../testing/test-data';
import { AuthService } from '../auth/auth.service';
import { isApiError } from './api-error.model';
import { errorInterceptor } from './error.interceptor';
import { retryInterceptor } from './retry.interceptor';

describe('retryInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([retryInterceptor, errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { logout: vi.fn() } },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    vi.useRealTimers();
  });

  it('retries a GET once after a transient 503 and succeeds', () => {
    let result: unknown;
    http.get('/api/characters').subscribe((value) => (result = value));

    controller
      .expectOne('/api/characters')
      .flush(anApiErrorBody({ status: 503, error: 'SERVICE_UNAVAILABLE' }), { status: 503, statusText: 'Unavailable' });
    vi.advanceTimersByTime(750);
    controller.expectOne('/api/characters').flush({ content: [] });

    expect(result).toEqual({ content: [] });
  });

  it('gives up after the second transient failure', () => {
    let captured: unknown;
    http.get('/api/characters').subscribe({ error: (error: unknown) => (captured = error) });

    controller.expectOne('/api/characters').error(new ProgressEvent('error'), { status: 0 });
    vi.advanceTimersByTime(750);
    controller.expectOne('/api/characters').error(new ProgressEvent('error'), { status: 0 });

    expect(isApiError(captured) && captured.code).toBe('NETWORK_ERROR');
  });

  it('does not retry a 404 nor a non-GET request', () => {
    let captured: unknown;
    http.get('/api/characters/999').subscribe({ error: (error: unknown) => (captured = error) });
    controller.expectOne('/api/characters/999').flush(anApiErrorBody(), { status: 404, statusText: 'Not Found' });
    vi.advanceTimersByTime(750);
    expect(isApiError(captured) && captured.status).toBe(404);

    let postError: unknown;
    http.post('/api/users/me/favorites/1', null).subscribe({ error: (error: unknown) => (postError = error) });
    controller.expectOne('/api/users/me/favorites/1').error(new ProgressEvent('error'), { status: 0 });
    vi.advanceTimersByTime(750);
    expect(isApiError(postError) && postError.code).toBe('NETWORK_ERROR');
  });
});

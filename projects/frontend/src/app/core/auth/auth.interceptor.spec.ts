import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  const token = signal<string | null>(null);
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    token.set(null);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { token } },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('adds the bearer token to API requests', () => {
    token.set('abc');
    http.get('/api/users/me/favorites').subscribe();
    const req = controller.expectOne('/api/users/me/favorites');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush([]);
  });

  it('sends no header when there is no session', () => {
    http.get('/api/characters').subscribe();
    const req = controller.expectOne('/api/characters');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('never attaches the token to login or register', () => {
    token.set('abc');
    http.post('/api/auth/login', {}).subscribe();
    const req = controller.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('never leaks the token to a non-API url', () => {
    token.set('abc');
    http.get('https://rickandmortyapi.com/api/character').subscribe();
    const req = controller.expectOne('https://rickandmortyapi.com/api/character');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});

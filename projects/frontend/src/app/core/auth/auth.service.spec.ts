import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BrowserStorage } from '../storage/browser-storage';
import { InMemoryStorage } from '../../../testing/in-memory-storage';
import { isoInMinutes } from '../../../testing/test-data';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let storage: InMemoryStorage;

  function setup(): { auth: AuthService; http: HttpTestingController } {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BrowserStorage, useValue: storage },
      ],
    });
    return { auth: TestBed.inject(AuthService), http: TestBed.inject(HttpTestingController) };
  }

  beforeEach(() => {
    storage = new InMemoryStorage();
  });

  it('starts without a session when nothing is stored', () => {
    const { auth } = setup();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.sessionStatus()).toBe('none');
  });

  it('restores a stored session that has not expired', () => {
    storage.set('rm.session', JSON.stringify({ token: 't', username: 'rick', expiresAt: isoInMinutes(30) }));
    const { auth } = setup();
    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.username()).toBe('rick');
    expect(auth.token()).toBe('t');
  });

  it('discards a stored session that already expired', () => {
    storage.set('rm.session', JSON.stringify({ token: 't', username: 'rick', expiresAt: isoInMinutes(-1) }));
    const { auth } = setup();
    expect(auth.isAuthenticated()).toBe(false);
    expect(storage.get('rm.session')).toBeNull();
  });

  it('ignores a corrupt stored value', () => {
    storage.set('rm.session', '{not json');
    const { auth } = setup();
    expect(auth.isAuthenticated()).toBe(false);
  });

  it('login persists the session returned by the backend', () => {
    const { auth, http } = setup();
    let username = '';
    auth.login({ username: 'rick', password: 'wubbalubba' }).subscribe((session) => (username = session.username));

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'rick', password: 'wubbalubba' });
    req.flush({ token: 'abc', expiresAt: isoInMinutes(60), username: 'rick' });

    expect(username).toBe('rick');
    expect(auth.isAuthenticated()).toBe(true);
    expect(JSON.parse(storage.get('rm.session') ?? '{}')).toMatchObject({ token: 'abc', username: 'rick' });
    http.verify();
  });

  it('logout clears both the signal and the storage', () => {
    storage.set('rm.session', JSON.stringify({ token: 't', username: 'rick', expiresAt: isoInMinutes(30) }));
    const { auth } = setup();
    auth.logout();
    expect(auth.isAuthenticated()).toBe(false);
    expect(storage.get('rm.session')).toBeNull();
  });

  it('reports "expired" and clears the session when the expiry is reached after start', () => {
    storage.set('rm.session', JSON.stringify({ token: 't', username: 'rick', expiresAt: isoInMinutes(1) }));
    const { auth } = setup();
    vi.useFakeTimers();
    try {
      vi.setSystemTime(Date.now() + 2 * 60_000);
      expect(auth.sessionStatus()).toBe('expired');
      expect(auth.isAuthenticated()).toBe(false);
    } finally {
      vi.useRealTimers();
    }
  });
});

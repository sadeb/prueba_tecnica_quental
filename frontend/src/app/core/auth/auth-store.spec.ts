import { AuthStore } from './auth-store';

describe('AuthStore', () => {
  beforeEach(() => localStorage.clear());

  it('persists an active opaque-token session and clears it explicitly', () => {
    const store = new AuthStore();
    store.setSession({
      token: 'opaque-token-value',
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      user: { id: 7, username: 'summer', role: 'USER' },
    });

    expect(store.isAuthenticated()).toBeTrue();
    expect(store.user()?.username).toBe('summer');
    expect(localStorage.getItem('quental.rickmorty.session')).toContain('opaque-token-value');

    store.clear();
    expect(store.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('quental.rickmorty.session')).toBeNull();
  });

  it('discards an expired session during initialization', () => {
    localStorage.setItem(
      'quental.rickmorty.session',
      JSON.stringify({
        token: 'expired-token',
        expiresAt: new Date(Date.now() - 1_000).toISOString(),
        user: { id: 8, username: 'morty', role: 'USER' },
      }),
    );

    const store = new AuthStore();

    expect(store.session()).toBeNull();
    expect(localStorage.getItem('quental.rickmorty.session')).toBeNull();
  });
});

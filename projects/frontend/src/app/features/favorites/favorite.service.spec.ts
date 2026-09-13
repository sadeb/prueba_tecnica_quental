import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { aCharacter, anApiErrorBody } from '../../../testing/test-data';
import { AuthService } from '../../core/auth/auth.service';
import { FavoriteService } from './favorite.service';

describe('FavoriteService', () => {
  const isAuthenticated = signal(true);
  let service: FavoriteService;
  let controller: HttpTestingController;

  beforeEach(() => {
    isAuthenticated.set(true);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: AuthService, useValue: { isAuthenticated } }],
    });
    service = TestBed.inject(FavoriteService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('load fills the id set from the backend list', () => {
    service.load().subscribe();
    controller.expectOne('/api/users/me/favorites').flush([aCharacter({ id: 1 }), aCharacter({ id: 5 })]);
    expect(service.loaded()).toBe(true);
    expect(service.count()).toBe(2);
    expect(service.isFavorite(5)).toBe(true);
    expect(service.isFavorite(2)).toBe(false);
  });

  it('add is optimistic and stays when the backend confirms', () => {
    service.add(3).subscribe();
    expect(service.isFavorite(3)).toBe(true);
    expect(service.isPending(3)).toBe(true);
    const req = controller.expectOne('/api/users/me/favorites/3');
    expect(req.request.method).toBe('POST');
    req.flush(aCharacter({ id: 3 }), { status: 201, statusText: 'Created' });
    expect(service.isFavorite(3)).toBe(true);
    expect(service.isPending(3)).toBe(false);
  });

  it('add is reverted when the backend rejects it', () => {
    let failed = false;
    service.add(999).subscribe({ error: () => (failed = true) });
    expect(service.isFavorite(999)).toBe(true);
    controller.expectOne('/api/users/me/favorites/999').flush(anApiErrorBody(), { status: 404, statusText: 'Not Found' });
    expect(failed).toBe(true);
    expect(service.isFavorite(999)).toBe(false);
    expect(service.isPending(999)).toBe(false);
  });

  it('remove is optimistic and restored on error', () => {
    service.load().subscribe();
    controller.expectOne('/api/users/me/favorites').flush([aCharacter({ id: 1 })]);

    service.remove(1).subscribe({ error: () => undefined });
    expect(service.isFavorite(1)).toBe(false);
    const req = controller.expectOne('/api/users/me/favorites/1');
    expect(req.request.method).toBe('DELETE');
    req.error(new ProgressEvent('error'), { status: 0 });
    expect(service.isFavorite(1)).toBe(true);
  });

  it('toggle removes when already favorite and adds otherwise', () => {
    service.toggle(4).subscribe();
    controller.expectOne('/api/users/me/favorites/4').flush(aCharacter({ id: 4 }));
    expect(service.isFavorite(4)).toBe(true);
    service.toggle(4).subscribe();
    const req = controller.expectOne('/api/users/me/favorites/4');
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
    expect(service.isFavorite(4)).toBe(false);
  });

  it('forgets everything when the session ends', () => {
    service.load().subscribe();
    controller.expectOne('/api/users/me/favorites').flush([aCharacter({ id: 1 })]);
    isAuthenticated.set(false);
    TestBed.tick();
    expect(service.count()).toBe(0);
    expect(service.loaded()).toBe(false);
  });
});

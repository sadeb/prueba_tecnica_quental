import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UsersApi } from './users-api';

describe('UsersApi', () => {
  const user = {
    id: 12,
    username: 'new.operator',
    role: 'USER' as const,
    enabled: true,
    createdAt: '2026-09-13T12:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('creates a user through the administrative endpoint', () => {
    const api = TestBed.inject(UsersApi);
    const http = TestBed.inject(HttpTestingController);
    let createdUsername = '';

    api.create({ username: 'new.operator', password: 'secret-123' }).subscribe((user) => {
      createdUsername = user.username;
    });

    const request = http.expectOne('/api/v1/admin/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'new.operator', password: 'secret-123' });
    request.flush(user);

    expect(createdUsername).toBe('new.operator');
    http.verify();
  });

  it('lists a filtered page of users', () => {
    const api = TestBed.inject(UsersApi);
    const http = TestBed.inject(HttpTestingController);
    let totalElements = 0;

    api
      .list({ search: 'operator', page: 1, size: 10, sort: 'createdAt', direction: 'desc' })
      .subscribe((page) => {
        totalElements = page.totalElements;
      });

    const request = http.expectOne(
      '/api/v1/admin/users?page=1&size=10&search=operator&sort=createdAt&direction=desc',
    );
    expect(request.request.method).toBe('GET');
    request.flush({ content: [user], page: 1, size: 10, totalElements: 11, totalPages: 2 });

    expect(totalElements).toBe(11);
    http.verify();
  });

  it('updates a user through the administrative endpoint', () => {
    const api = TestBed.inject(UsersApi);
    const http = TestBed.inject(HttpTestingController);
    const update = { username: 'renamed.operator', enabled: false };

    api.update(12, update).subscribe();

    const request = http.expectOne('/api/v1/admin/users/12');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(update);
    request.flush({ ...user, username: update.username, enabled: false });
    http.verify();
  });

  it('deletes a user through the administrative endpoint', () => {
    const api = TestBed.inject(UsersApi);
    const http = TestBed.inject(HttpTestingController);

    api.delete(12).subscribe();

    const request = http.expectOne('/api/v1/admin/users/12');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    http.verify();
  });
});

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UsersApi } from './users-api';

describe('UsersApi', () => {
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
    request.flush({ id: 12, username: 'new.operator', role: 'USER' });

    expect(createdUsername).toBe('new.operator');
    http.verify();
  });
});

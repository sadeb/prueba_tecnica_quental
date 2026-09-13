import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateUserRequest, User } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UsersApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/admin/users';

  create(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }
}

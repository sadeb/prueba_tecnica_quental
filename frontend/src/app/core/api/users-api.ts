import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.models';
import {
  AdminUser,
  CreateUserRequest,
  UpdateUserRequest,
  UserListQuery,
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UsersApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/admin/users';

  list(query: UserListQuery = {}): Observable<PageResponse<AdminUser>> {
    const params: Record<string, string | number> = {
      page: query.page ?? 0,
      size: query.size ?? 20,
    };
    const search = query.search?.trim();
    if (search) params['search'] = search;
    if (query.sort) params['sort'] = query.sort;
    if (query.direction) params['direction'] = query.direction;
    return this.http.get<PageResponse<AdminUser>>(this.baseUrl, { params });
  }

  create(request: CreateUserRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.baseUrl, request);
  }

  update(id: number, request: UpdateUserRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

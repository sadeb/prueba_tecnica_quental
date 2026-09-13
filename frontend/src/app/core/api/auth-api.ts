import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthStore } from '../auth/auth-store';
import { LoginRequest, TokenResponse, User } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly authStore = inject(AuthStore);
  private readonly baseUrl = '/api/v1/auth';

  login(request: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${this.baseUrl}/login`, request)
      .pipe(tap((session) => this.authStore.setSession(session)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.baseUrl}/logout`, {})
      .pipe(tap({ next: () => this.authStore.clear(), error: () => this.authStore.clear() }));
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`);
  }
}

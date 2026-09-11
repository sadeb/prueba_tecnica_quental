import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.models';
import { CharacterSummary } from '../models/catalog.models';

@Injectable({ providedIn: 'root' })
export class FavoritesApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/users/me/favorites';

  list(page = 0, size = 20): Observable<PageResponse<CharacterSummary>> {
    return this.http.get<PageResponse<CharacterSummary>>(this.baseUrl, { params: { page, size } });
  }

  add(characterId: number): Observable<CharacterSummary> {
    return this.http.put<CharacterSummary>(`${this.baseUrl}/${characterId}`, {});
  }

  remove(characterId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${characterId}`);
  }
}

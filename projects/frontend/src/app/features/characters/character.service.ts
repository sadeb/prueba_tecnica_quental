import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CharacterDetail,
  CharacterFilters,
  CharacterSummary,
  RelatedCharacter,
} from '../../shared/models/character.model';
import { PageResponse } from '../../shared/models/page-response.model';

/** HTTP access to `/api/characters*`. Filters without value are never sent. */
@Service()
export class CharacterService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/characters`;

  search(
    filters: CharacterFilters,
    page: number,
    size: number = environment.pageSize,
  ): Observable<PageResponse<CharacterSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    for (const [key, value] of Object.entries(filters)) {
      const trimmed = value.trim();
      if (trimmed !== '') {
        params = params.set(key, trimmed);
      }
    }
    return this.http.get<PageResponse<CharacterSummary>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<CharacterDetail> {
    return this.http.get<CharacterDetail>(`${this.baseUrl}/${id}`);
  }

  getRelated(id: number, limit: number = environment.relatedLimit): Observable<RelatedCharacter[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<RelatedCharacter[]>(`${this.baseUrl}/${id}/related`, { params });
  }
}

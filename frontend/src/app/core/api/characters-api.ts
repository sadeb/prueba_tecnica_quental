import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.models';
import {
  CharacterDetail,
  CharacterFilters,
  CharacterSummary,
  RelatedCharacter,
} from '../models/catalog.models';

@Injectable({ providedIn: 'root' })
export class CharactersApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/characters';

  search(filters: CharacterFilters): Observable<PageResponse<CharacterSummary>> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<PageResponse<CharacterSummary>>(this.baseUrl, { params });
  }

  detail(id: number): Observable<CharacterDetail> {
    return this.http.get<CharacterDetail>(`${this.baseUrl}/${id}`);
  }

  related(id: number, limit = 10): Observable<RelatedCharacter[]> {
    return this.http.get<RelatedCharacter[]>(`${this.baseUrl}/${id}/related`, {
      params: { limit },
    });
  }
}

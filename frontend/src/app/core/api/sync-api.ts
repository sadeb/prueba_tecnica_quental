import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.models';
import { SyncRun } from '../models/sync.models';

@Injectable({ providedIn: 'root' })
export class SyncApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/admin/sync-runs';

  start(): Observable<SyncRun> {
    return this.http.post<SyncRun>(this.baseUrl, {});
  }

  list(page = 0, size = 20): Observable<PageResponse<SyncRun>> {
    return this.http.get<PageResponse<SyncRun>>(this.baseUrl, { params: { page, size } });
  }
}

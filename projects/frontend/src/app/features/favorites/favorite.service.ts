import { HttpClient } from '@angular/common/http';
import { computed, effect, inject, Service, signal } from '@angular/core';
import { catchError, finalize, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth/auth.service';
import { CharacterSummary } from '../../shared/models/character.model';

/**
 * Favorites of the signed-in user. Keeps the set of ids so every card can show its state, and
 * applies optimistic updates that are reverted when the backend rejects the change.
 */
@Service()
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = `${environment.apiBaseUrl}/users/me/favorites`;

  private readonly idsSignal = signal<ReadonlySet<number>>(new Set());
  private readonly pendingSignal = signal<ReadonlySet<number>>(new Set());
  private readonly loadedSignal = signal(false);

  readonly ids = this.idsSignal.asReadonly();
  readonly loaded = this.loadedSignal.asReadonly();
  readonly count = computed(() => this.idsSignal().size);

  constructor() {
    effect(() => {
      if (!this.auth.isAuthenticated()) {
        this.reset();
      }
    });
  }

  isFavorite(characterId: number): boolean {
    return this.idsSignal().has(characterId);
  }

  isPending(characterId: number): boolean {
    return this.pendingSignal().has(characterId);
  }

  load(): Observable<CharacterSummary[]> {
    return this.http.get<CharacterSummary[]>(this.baseUrl).pipe(
      tap((favorites) => {
        this.idsSignal.set(new Set(favorites.map((favorite) => favorite.id)));
        this.loadedSignal.set(true);
      }),
    );
  }

  add(characterId: number): Observable<CharacterSummary> {
    this.setFavorite(characterId, true);
    this.setPending(characterId, true);
    return this.http.post<CharacterSummary>(`${this.baseUrl}/${characterId}`, null).pipe(
      catchError((error: unknown) => {
        this.setFavorite(characterId, false);
        return throwError(() => error);
      }),
      finalize(() => this.setPending(characterId, false)),
    );
  }

  remove(characterId: number): Observable<void> {
    this.setFavorite(characterId, false);
    this.setPending(characterId, true);
    return this.http.delete<void>(`${this.baseUrl}/${characterId}`).pipe(
      catchError((error: unknown) => {
        this.setFavorite(characterId, true);
        return throwError(() => error);
      }),
      finalize(() => this.setPending(characterId, false)),
    );
  }

  toggle(characterId: number): Observable<CharacterSummary | void> {
    return this.isFavorite(characterId) ? this.remove(characterId) : this.add(characterId);
  }

  reset(): void {
    this.idsSignal.set(new Set());
    this.pendingSignal.set(new Set());
    this.loadedSignal.set(false);
  }

  private setFavorite(characterId: number, favorite: boolean): void {
    this.idsSignal.update((ids) => {
      const next = new Set(ids);
      if (favorite) {
        next.add(characterId);
      } else {
        next.delete(characterId);
      }
      return next;
    });
  }

  private setPending(characterId: number, pending: boolean): void {
    this.pendingSignal.update((ids) => {
      const next = new Set(ids);
      if (pending) {
        next.add(characterId);
      } else {
        next.delete(characterId);
      }
      return next;
    });
  }
}

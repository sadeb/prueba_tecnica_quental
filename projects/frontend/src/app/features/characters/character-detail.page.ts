import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, Observable, of, startWith, Subject, switchMap, tap } from 'rxjs';
import { ApiError, isApiError } from '../../core/errors/api-error.model';
import { describeError } from '../../core/errors/error-messages';
import { ToastService } from '../../core/notifications/toast.service';
import { APP_NAME } from '../../core/routing/app-title.strategy';
import { genderLabel, pluralize } from '../../shared/i18n/labels';
import { CharacterDetail, RelatedCharacter } from '../../shared/models/character.model';
import { viewState, ViewState } from '../../shared/models/view-state.model';
import { CharacterCardComponent } from '../../shared/ui/character-card.component';
import { CharacterImageComponent } from '../../shared/ui/character-image.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { ErrorAlertComponent } from '../../shared/ui/error-alert.component';
import { FavoriteToggleComponent } from '../../shared/ui/favorite-toggle.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
import { FavoriteService } from '../favorites/favorite.service';
import { CharacterService } from './character.service';

/**
 * Detail and related characters are loaded independently: Neo4j being down must not hide the
 * character itself, and each block can be retried on its own.
 */
@Component({
  selector: 'app-character-detail-page',
  imports: [
    RouterLink,
    CharacterImageComponent,
    StatusBadgeComponent,
    FavoriteToggleComponent,
    CharacterCardComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    ErrorAlertComponent,
  ],
  templateUrl: './character-detail.page.html',
})
export class CharacterDetailPage {
  private readonly route = inject(ActivatedRoute);
  private readonly characters = inject(CharacterService);
  private readonly toasts = inject(ToastService);
  private readonly title = inject(Title);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reloadDetail$ = new Subject<void>();
  private readonly reloadRelated$ = new Subject<void>();

  protected readonly favorites = inject(FavoriteService);
  protected readonly id = signal<number | null>(null);
  protected readonly detail = signal<ViewState<CharacterDetail>>(viewState.loading());
  protected readonly related = signal<ViewState<RelatedCharacter[]>>(viewState.loading());

  protected readonly character = computed(() => {
    const state = this.detail();
    return state.status === 'ready' ? state.data : null;
  });
  protected readonly isFavorite = computed(() => {
    const id = this.id();
    return id !== null && this.favorites.isFavorite(id);
  });
  protected readonly favoritePending = computed(() => {
    const id = this.id();
    return id !== null && this.favorites.isPending(id);
  });

  constructor() {
    const id$ = this.route.paramMap.pipe(
      map((params) => parseId(params.get('id'))),
      tap((id) => this.id.set(id)),
    );

    id$
      .pipe(
        switchMap((id) =>
          this.reloadDetail$.pipe(
            startWith(undefined),
            tap(() => this.detail.set(viewState.loading())),
            switchMap(() => this.loadDetail(id)),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((state) => {
        this.detail.set(state);
        if (state.status === 'ready') {
          this.title.setTitle(`${state.data.name} · ${APP_NAME}`);
        }
      });

    id$
      .pipe(
        switchMap((id) =>
          this.reloadRelated$.pipe(
            startWith(undefined),
            tap(() => this.related.set(viewState.loading())),
            switchMap(() => this.loadRelated(id)),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.related.set(state));
  }

  protected reloadDetail(): void {
    this.reloadDetail$.next();
  }

  protected reloadRelated(): void {
    this.reloadRelated$.next();
  }

  protected onFavoriteToggle(): void {
    const character = this.character();
    if (character === null) {
      return;
    }
    const wasFavorite = this.favorites.isFavorite(character.id);
    this.favorites
      .toggle(character.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () =>
          this.toasts.success(
            wasFavorite
              ? `${character.name} ya no está en tus favoritos.`
              : `${character.name} añadido a tus favoritos.`,
          ),
        error: (error: unknown) =>
          this.toasts.error(
            isApiError(error) ? describeError(error, 'favorites') : 'No se ha podido actualizar el favorito.',
          ),
      });
  }

  protected onRelatedFavoriteToggle(characterId: number, name: string): void {
    const wasFavorite = this.favorites.isFavorite(characterId);
    this.favorites
      .toggle(characterId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () =>
          this.toasts.success(
            wasFavorite ? `${name} ya no está en tus favoritos.` : `${name} añadido a tus favoritos.`,
          ),
        error: (error: unknown) =>
          this.toasts.error(
            isApiError(error) ? describeError(error, 'favorites') : 'No se ha podido actualizar el favorito.',
          ),
      });
  }

  protected genderLabel(character: CharacterDetail): string {
    return genderLabel(character.gender);
  }

  protected episodesLabel(character: CharacterDetail): string {
    return pluralize(character.episodes.length, 'episodio', 'episodios');
  }

  protected sharedLabel(related: RelatedCharacter): string {
    return pluralize(related.sharedEpisodes, 'episodio en común', 'episodios en común');
  }

  private loadDetail(id: number | null): Observable<ViewState<CharacterDetail>> {
    if (id === null) {
      return of(viewState.error<CharacterDetail>(invalidIdError()));
    }
    return this.characters.getById(id).pipe(
      map((detail) => viewState.ready(detail)),
      catchError((error: unknown) => of(viewState.error<CharacterDetail>(asApiError(error)))),
    );
  }

  private loadRelated(id: number | null): Observable<ViewState<RelatedCharacter[]>> {
    if (id === null) {
      return of(viewState.empty<RelatedCharacter[]>());
    }
    return this.characters.getRelated(id).pipe(
      map((related) =>
        related.length > 0 ? viewState.ready(related) : viewState.empty<RelatedCharacter[]>(),
      ),
      catchError((error: unknown) => of(viewState.error<RelatedCharacter[]>(asApiError(error)))),
    );
  }
}

function parseId(raw: string | null): number | null {
  if (raw === null || !/^\d+$/.test(raw)) {
    return null;
  }
  const id = Number(raw);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function invalidIdError(): ApiError {
  return new ApiError({
    status: 404,
    code: 'NOT_FOUND',
    userMessage: 'El identificador del personaje no es válido.',
  });
}

function asApiError(error: unknown): ApiError {
  return isApiError(error)
    ? error
    : new ApiError({ status: 0, code: 'UNKNOWN_ERROR', userMessage: 'Ha ocurrido un error inesperado.' });
}

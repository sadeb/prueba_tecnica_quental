import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, combineLatest, map, of, startWith, Subject, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiError, isApiError } from '../../core/errors/api-error.model';
import { describeError } from '../../core/errors/error-messages';
import { ToastService } from '../../core/notifications/toast.service';
import { pluralize } from '../../shared/i18n/labels';
import {
  CharacterFilters,
  CharacterListQuery,
  CharacterSummary,
  EMPTY_FILTERS,
  queryFromParams,
} from '../../shared/models/character.model';
import { PageResponse } from '../../shared/models/page-response.model';
import { viewState, ViewState } from '../../shared/models/view-state.model';
import { CharacterCardComponent } from '../../shared/ui/character-card.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { ErrorAlertComponent } from '../../shared/ui/error-alert.component';
import { PaginationComponent } from '../../shared/ui/pagination.component';
import { SkeletonCardComponent } from '../../shared/ui/skeleton-card.component';
import { FavoriteService } from '../favorites/favorite.service';
import { CharacterFiltersComponent } from './character-filters.component';
import { CharacterService } from './character.service';

/** Filters and page live in the URL: reloading, sharing a link and the back button all work. */
@Component({
  selector: 'app-character-list-page',
  imports: [
    CharacterFiltersComponent,
    CharacterCardComponent,
    SkeletonCardComponent,
    EmptyStateComponent,
    ErrorAlertComponent,
    PaginationComponent,
  ],
  templateUrl: './character-list.page.html',
})
export class CharacterListPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly characters = inject(CharacterService);
  private readonly toasts = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  protected readonly favorites = inject(FavoriteService);
  protected readonly skeletons = Array.from({ length: environment.pageSize }, (_, index) => index);
  protected readonly query = signal<CharacterListQuery>({ filters: EMPTY_FILTERS, page: 0 });
  protected readonly state = signal<ViewState<PageResponse<CharacterSummary>>>(viewState.loading());

  constructor() {
    const query$ = this.route.queryParamMap.pipe(
      map(queryFromParams),
      tap((query) => this.query.set(query)),
    );
    combineLatest([query$, this.reload$.pipe(startWith(undefined))])
      .pipe(
        tap(() => this.state.set(viewState.loading())),
        switchMap(([query]) => this.load(query)),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.state.set(state));
  }

  protected onFiltersChange(filters: CharacterFilters): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        name: filters.name.trim() || null,
        status: filters.status || null,
        species: filters.species.trim() || null,
        gender: filters.gender || null,
        page: null,
      },
      queryParamsHandling: 'merge',
    });
  }

  protected onPageChange(page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page > 0 ? page : null },
      queryParamsHandling: 'merge',
    });
  }

  protected clearFilters(): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: {} });
  }

  protected reload(): void {
    this.reload$.next();
  }

  protected onFavoriteToggle(character: CharacterSummary): void {
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
        error: (error: unknown) => this.toasts.error(this.favoriteErrorMessage(error)),
      });
  }

  protected resultsLabel(page: PageResponse<CharacterSummary>): string {
    return pluralize(page.totalElements, 'personaje encontrado', 'personajes encontrados');
  }

  private load(query: CharacterListQuery) {
    return this.characters.search(query.filters, query.page).pipe(
      map((page) =>
        page.content.length > 0
          ? viewState.ready<PageResponse<CharacterSummary>>(page)
          : viewState.empty<PageResponse<CharacterSummary>>(),
      ),
      catchError((error: unknown) =>
        of(viewState.error<PageResponse<CharacterSummary>>(this.asApiError(error))),
      ),
    );
  }

  private asApiError(error: unknown): ApiError {
    return isApiError(error)
      ? error
      : new ApiError({ status: 0, code: 'UNKNOWN_ERROR', userMessage: 'Ha ocurrido un error inesperado.' });
  }

  private favoriteErrorMessage(error: unknown): string {
    return isApiError(error)
      ? describeError(error, 'favorites')
      : 'No se ha podido actualizar el favorito.';
  }
}

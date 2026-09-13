import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith, Subject, switchMap, tap } from 'rxjs';
import { ApiError, isApiError } from '../../core/errors/api-error.model';
import { describeError } from '../../core/errors/error-messages';
import { ToastService } from '../../core/notifications/toast.service';
import { pluralize } from '../../shared/i18n/labels';
import { CharacterSummary } from '../../shared/models/character.model';
import { viewState, ViewState } from '../../shared/models/view-state.model';
import { CharacterCardComponent } from '../../shared/ui/character-card.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { ErrorAlertComponent } from '../../shared/ui/error-alert.component';
import { SkeletonCardComponent } from '../../shared/ui/skeleton-card.component';
import { FavoriteService } from './favorite.service';

@Component({
  selector: 'app-favorites-page',
  imports: [RouterLink, CharacterCardComponent, SkeletonCardComponent, EmptyStateComponent, ErrorAlertComponent],
  templateUrl: './favorites.page.html',
})
export class FavoritesPage {
  private readonly toasts = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  protected readonly favorites = inject(FavoriteService);
  protected readonly skeletons = [0, 1, 2, 3];
  protected readonly state = signal<ViewState<readonly CharacterSummary[]>>(viewState.loading());

  constructor() {
    this.reload$
      .pipe(
        startWith(undefined),
        tap(() => this.state.set(viewState.loading())),
        switchMap(() =>
          this.favorites.load().pipe(
            map((list) =>
              list.length > 0
                ? viewState.ready<readonly CharacterSummary[]>(list)
                : viewState.empty<readonly CharacterSummary[]>(),
            ),
            catchError((error: unknown) =>
              of(viewState.error<readonly CharacterSummary[]>(asApiError(error))),
            ),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.state.set(state));
  }

  protected reload(): void {
    this.reload$.next();
  }

  protected remove(character: CharacterSummary): void {
    const previous = this.state();
    // Optimistic: the card disappears at once; it comes back if the backend says no.
    this.state.update((state) => {
      if (state.status !== 'ready') {
        return state;
      }
      const remaining = state.data.filter((item) => item.id !== character.id);
      return remaining.length > 0 ? viewState.ready(remaining) : viewState.empty();
    });
    this.favorites
      .remove(character.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.toasts.success(`${character.name} eliminado de tus favoritos.`),
        error: (error: unknown) => {
          this.state.set(previous);
          this.toasts.error(
            isApiError(error) ? describeError(error, 'favorites') : 'No se ha podido eliminar el favorito.',
          );
        },
      });
  }

  protected countLabel(list: readonly CharacterSummary[]): string {
    return pluralize(list.length, 'personaje favorito', 'personajes favoritos');
  }
}

function asApiError(error: unknown): ApiError {
  return isApiError(error)
    ? error
    : new ApiError({ status: 0, code: 'UNKNOWN_ERROR', userMessage: 'Ha ocurrido un error inesperado.' });
}

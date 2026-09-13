import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form } from '@angular/forms/signals';
import { finalize, Observable } from 'rxjs';
import { CharactersApi } from '../../core/api/characters-api';
import { FavoritesApi } from '../../core/api/favorites-api';
import { apiErrorMessage } from '../../core/http/api-error-message';
import { PageResponse } from '../../core/models/api.models';
import { CharacterSummary } from '../../core/models/catalog.models';
import { CharacterCard } from '../../shared/character-card/character-card';

@Component({
  selector: 'app-character-list-page',
  imports: [FormField, CharacterCard],
  templateUrl: './character-list-page.html',
  styleUrl: './character-list-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CharacterListPage {
  private readonly charactersApi = inject(CharactersApi);
  private readonly favoritesApi = inject(FavoritesApi);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly result = signal<PageResponse<CharacterSummary> | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly favoriteNotice = signal('');
  protected readonly favoriteIds = signal<ReadonlySet<number>>(new Set());
  protected readonly favoritesLoading = signal(true);
  protected readonly pendingFavoriteIds = signal<ReadonlySet<number>>(new Set());
  protected readonly filters = signal({ name: '', status: '', species: '', gender: '' });
  protected readonly filterForm = form(this.filters);

  constructor() {
    this.loadFavoriteIds();
    this.load(0);
  }

  protected search(event: SubmitEvent): void {
    event.preventDefault();
    this.load(0);
  }

  protected clearFilters(): void {
    this.filters.set({ name: '', status: '', species: '', gender: '' });
    this.load(0);
  }

  protected load(page: number): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.charactersApi
      .search({ ...this.filters(), page, size: 20 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.result.set(result);
          this.loading.set(false);
        },
        error: (error) => {
          this.errorMessage.set(apiErrorMessage(error, 'No fue posible cargar el catálogo.'));
          this.loading.set(false);
        },
      });
  }

  protected isFavorite(characterId: number): boolean {
    return this.favoriteIds().has(characterId);
  }

  protected toggleFavorite(character: CharacterSummary): void {
    if (this.pendingFavoriteIds().has(character.id)) {
      return;
    }

    const removing = this.isFavorite(character.id);
    const request: Observable<unknown> = removing
      ? this.favoritesApi.remove(character.id)
      : this.favoritesApi.add(character.id);

    this.setFavoritePending(character.id, true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setFavoritePending(character.id, false)),
      )
      .subscribe({
        next: () => {
          this.setFavorite(character.id, !removing);
          this.favoriteNotice.set(
            removing
              ? `${character.name} se retiró de favoritos.`
              : `${character.name} quedó guardado en favoritos.`,
          );
        },
        error: (error) =>
          this.favoriteNotice.set(
            apiErrorMessage(
              error,
              removing
                ? 'No fue posible retirar el favorito.'
                : 'No fue posible guardar el favorito.',
            ),
          ),
      });
  }

  private loadFavoriteIds(): void {
    this.favoritesApi
      .listAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (favorites) => {
          this.favoriteIds.set(new Set(favorites.map(({ id }) => id)));
          this.favoritesLoading.set(false);
        },
        error: (error) => {
          this.favoriteNotice.set(
            apiErrorMessage(error, 'No fue posible recuperar el estado de favoritos.'),
          );
          this.favoritesLoading.set(false);
        },
      });
  }

  private setFavorite(characterId: number, favorite: boolean): void {
    const next = new Set(this.favoriteIds());
    if (favorite) {
      next.add(characterId);
    } else {
      next.delete(characterId);
    }
    this.favoriteIds.set(next);
  }

  private setFavoritePending(characterId: number, pending: boolean): void {
    const next = new Set(this.pendingFavoriteIds());
    if (pending) {
      next.add(characterId);
    } else {
      next.delete(characterId);
    }
    this.pendingFavoriteIds.set(next);
  }
}

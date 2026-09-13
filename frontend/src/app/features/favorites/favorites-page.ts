import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { FavoritesApi } from '../../core/api/favorites-api';
import { apiErrorMessage } from '../../core/http/api-error-message';
import { PageResponse } from '../../core/models/api.models';
import { CharacterSummary } from '../../core/models/catalog.models';
import { CharacterCard } from '../../shared/character-card/character-card';

@Component({
  selector: 'app-favorites-page',
  imports: [CharacterCard],
  templateUrl: './favorites-page.html',
  styleUrl: './favorites-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FavoritesPage {
  private readonly api = inject(FavoritesApi);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly result = signal<PageResponse<CharacterSummary> | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly pendingFavoriteIds = signal<ReadonlySet<number>>(new Set());

  constructor() { this.load(0); }

  protected load(page: number): void {
    this.loading.set(true); this.errorMessage.set('');
    this.api.list(page).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (result) => { this.result.set(result); this.loading.set(false); },
      error: (error) => { this.errorMessage.set(apiErrorMessage(error, 'No fue posible cargar favoritos.')); this.loading.set(false); },
    });
  }

  protected remove(character: CharacterSummary): void {
    if (this.pendingFavoriteIds().has(character.id)) return;

    this.setFavoritePending(character.id, true);
    this.api
      .remove(character.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setFavoritePending(character.id, false)),
      )
      .subscribe({
        next: () => this.load(this.result()?.page ?? 0),
        error: (error) =>
          this.errorMessage.set(apiErrorMessage(error, 'No fue posible retirar el favorito.')),
      });
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

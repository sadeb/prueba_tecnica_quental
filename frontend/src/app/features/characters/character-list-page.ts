import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form } from '@angular/forms/signals';
import { Router } from '@angular/router';
import { CharactersApi } from '../../core/api/characters-api';
import { FavoritesApi } from '../../core/api/favorites-api';
import { AuthStore } from '../../core/auth/auth-store';
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
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly result = signal<PageResponse<CharacterSummary> | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly favoriteNotice = signal('');
  protected readonly filters = signal({ name: '', status: '', species: '', gender: '' });
  protected readonly filterForm = form(this.filters);
  protected readonly isAuthenticated = this.authStore.isAuthenticated;

  constructor() {
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

  protected addFavorite(character: CharacterSummary): void {
    if (!this.authStore.isAuthenticated()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    this.favoritesApi.add(character.id).subscribe({
      next: () => this.favoriteNotice.set(`${character.name} quedó guardado en favoritos.`),
      error: (error) =>
        this.favoriteNotice.set(apiErrorMessage(error, 'No fue posible guardar el favorito.')),
    });
  }
}

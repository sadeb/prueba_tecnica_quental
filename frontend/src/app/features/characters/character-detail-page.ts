import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CharactersApi } from '../../core/api/characters-api';
import { FavoritesApi } from '../../core/api/favorites-api';
import { AuthStore } from '../../core/auth/auth-store';
import { apiErrorMessage } from '../../core/http/api-error-message';
import { CharacterDetail, RelatedCharacter } from '../../core/models/catalog.models';

@Component({
  selector: 'app-character-detail-page',
  imports: [RouterLink],
  templateUrl: './character-detail-page.html',
  styleUrl: './character-detail-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CharacterDetailPage {
  private readonly api = inject(CharactersApi);
  private readonly favoritesApi = inject(FavoritesApi);
  private readonly authStore = inject(AuthStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly detail = signal<CharacterDetail | null>(null);
  protected readonly related = signal<RelatedCharacter[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly relatedError = signal('');
  protected readonly favoriteNotice = signal('');
  protected readonly isAuthenticated = this.authStore.isAuthenticated;

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.detail(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (detail) => { this.detail.set(detail); this.loading.set(false); },
      error: (error) => { this.errorMessage.set(apiErrorMessage(error, 'No fue posible abrir el expediente.')); this.loading.set(false); },
    });
    this.api.related(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (related) => this.related.set(related),
      error: (error) => this.relatedError.set(apiErrorMessage(error, 'La proyección de relaciones no está disponible.')),
    });
  }

  protected addFavorite(): void {
    const character = this.detail()?.character;
    if (!character) return;
    this.favoritesApi.add(character.id).subscribe({
      next: () => this.favoriteNotice.set('Expediente añadido a favoritos.'),
      error: (error) => this.favoriteNotice.set(apiErrorMessage(error, 'No fue posible guardar el favorito.')),
    });
  }
}

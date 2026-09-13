import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { genderLabel } from '../i18n/labels';
import { CharacterSummary } from '../models/character.model';
import { CharacterImageComponent } from './character-image.component';
import { FavoriteToggleComponent } from './favorite-toggle.component';
import { StatusBadgeComponent } from './status-badge.component';

/** Presentational card used by the list, the favorites page and the related-characters block. */
@Component({
  selector: 'app-character-card',
  imports: [RouterLink, CharacterImageComponent, FavoriteToggleComponent, StatusBadgeComponent],
  host: { class: 'd-block h-100' },
  templateUrl: './character-card.component.html',
})
export class CharacterCardComponent {
  readonly character = input.required<CharacterSummary>();
  readonly isFavorite = input(false);
  readonly favoritePending = input(false);
  readonly showFavorite = input(true);
  /** Optional line under the name, e.g. "12 episodios en común". */
  readonly note = input('');
  readonly favoriteToggle = output<CharacterSummary>();

  protected readonly subtitle = computed(() => {
    const character = this.character();
    return [character.species, character.type].filter((part) => part !== null && part !== '').join(' · ');
  });
  protected readonly gender = computed(() => genderLabel(this.character().gender));
  protected readonly detailLink = computed(() => ['/characters', this.character().id]);
}

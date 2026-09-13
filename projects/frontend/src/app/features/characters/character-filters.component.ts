import { Component, computed, effect, input, linkedSignal, output, untracked } from '@angular/core';
import { debounce, form, FormField } from '@angular/forms/signals';
import { GENDER_OPTIONS, STATUS_OPTIONS } from '../../shared/i18n/labels';
import {
  CharacterFilters,
  EMPTY_FILTERS,
  hasActiveFilters,
  sameFilters,
} from '../../shared/models/character.model';

const TEXT_DEBOUNCE_MS = 350;

/**
 * Presentational filter bar. The model mirrors the `filters` input (query params) and every
 * change that differs from it is emitted; text fields are debounced, selects apply at once.
 */
@Component({
  selector: 'app-character-filters',
  imports: [FormField],
  templateUrl: './character-filters.component.html',
})
export class CharacterFiltersComponent {
  readonly filters = input.required<CharacterFilters>();
  readonly filtersChange = output<CharacterFilters>();

  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly genderOptions = GENDER_OPTIONS;

  protected readonly model = linkedSignal<CharacterFilters, CharacterFilters>({
    source: this.filters,
    computation: (filters) => filters,
  });

  protected readonly form = form(this.model, (schema) => {
    debounce(schema.name, TEXT_DEBOUNCE_MS);
    debounce(schema.species, TEXT_DEBOUNCE_MS);
  });

  protected readonly hasActive = computed(() => hasActiveFilters(this.model()));

  constructor() {
    effect(() => {
      const value = this.model();
      if (!sameFilters(value, untracked(this.filters))) {
        this.filtersChange.emit(value);
      }
    });
  }

  protected reset(): void {
    this.model.set(EMPTY_FILTERS);
  }
}

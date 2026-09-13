import { ApiError, ApiErrorBody } from '../app/core/errors/api-error.model';
import { CharacterDetail, CharacterSummary, RelatedCharacter } from '../app/shared/models/character.model';
import { PageResponse } from '../app/shared/models/page-response.model';

/** Minimal, explicit factories: each test overrides only what it cares about. */
export function aCharacter(overrides: Partial<CharacterSummary> = {}): CharacterSummary {
  return {
    id: 1,
    externalId: 1,
    name: 'Rick Sanchez',
    status: 'ALIVE',
    species: 'Human',
    type: null,
    gender: 'MALE',
    imageUrl: 'https://example.test/rick.jpeg',
    ...overrides,
  };
}

export function aCharacterDetail(overrides: Partial<CharacterDetail> = {}): CharacterDetail {
  return {
    ...aCharacter(),
    origin: { id: 1, externalId: 1, name: 'Earth (C-137)', type: 'Planet', dimension: 'Dimension C-137' },
    location: { id: 20, externalId: 20, name: 'Earth (Replacement Dimension)', type: 'Planet', dimension: null },
    episodes: [{ id: 1, externalId: 1, name: 'Pilot', code: 'S01E01', airDate: 'December 2, 2013' }],
    ...overrides,
  };
}

export function aRelated(overrides: Partial<RelatedCharacter> = {}): RelatedCharacter {
  return { character: aCharacter({ id: 2, externalId: 2, name: 'Morty Smith' }), sharedEpisodes: 51, ...overrides };
}

export function aPage<T>(content: readonly T[], overrides: Partial<PageResponse<T>> = {}): PageResponse<T> {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages: content.length === 0 ? 0 : 1,
    ...overrides,
  };
}

export function anApiErrorBody(overrides: Partial<ApiErrorBody> = {}): ApiErrorBody {
  return {
    timestamp: '2026-09-13T10:00:00Z',
    status: 404,
    error: 'NOT_FOUND',
    message: 'Character 999 not found',
    path: '/api/characters/999',
    ...overrides,
  };
}

export function anApiError(overrides: Partial<ConstructorParameters<typeof ApiError>[0]> = {}): ApiError {
  return new ApiError({ status: 500, code: 'INTERNAL_ERROR', userMessage: 'Error del servidor.', ...overrides });
}

/** A future / past ISO instant relative to now. */
export function isoInMinutes(minutes: number): string {
  return new Date(Date.now() + minutes * 60_000).toISOString();
}

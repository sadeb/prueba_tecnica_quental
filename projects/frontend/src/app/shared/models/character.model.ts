import { ParamMap } from '@angular/router';

export const CHARACTER_STATUSES = ['ALIVE', 'DEAD', 'UNKNOWN'] as const;
export type CharacterStatus = (typeof CHARACTER_STATUSES)[number];

export const CHARACTER_GENDERS = ['FEMALE', 'MALE', 'GENDERLESS', 'UNKNOWN'] as const;
export type CharacterGender = (typeof CHARACTER_GENDERS)[number];

export interface CharacterSummary {
  readonly id: number;
  readonly externalId: number;
  readonly name: string;
  readonly status: CharacterStatus;
  readonly species: string | null;
  readonly type: string | null;
  readonly gender: CharacterGender;
  readonly imageUrl: string | null;
}

export interface LocationSummary {
  readonly id: number;
  readonly externalId: number;
  readonly name: string;
  readonly type: string | null;
  readonly dimension: string | null;
}

export interface EpisodeSummary {
  readonly id: number;
  readonly externalId: number;
  readonly name: string;
  readonly code: string;
  readonly airDate: string | null;
}

export interface CharacterDetail extends CharacterSummary {
  readonly origin: LocationSummary | null;
  readonly location: LocationSummary | null;
  readonly episodes: readonly EpisodeSummary[];
}

export interface RelatedCharacter {
  readonly character: CharacterSummary;
  readonly sharedEpisodes: number;
}

/** Empty string = filter not applied (never sent to the API). */
export interface CharacterFilters {
  readonly name: string;
  readonly status: CharacterStatus | '';
  readonly species: string;
  readonly gender: CharacterGender | '';
}

export const EMPTY_FILTERS: CharacterFilters = { name: '', status: '', species: '', gender: '' };

export interface CharacterListQuery {
  readonly filters: CharacterFilters;
  readonly page: number;
}

function isStatus(value: string): value is CharacterStatus {
  return (CHARACTER_STATUSES as readonly string[]).includes(value);
}

function isGender(value: string): value is CharacterGender {
  return (CHARACTER_GENDERS as readonly string[]).includes(value);
}

/** Query params are user input: unknown enum values or negative pages degrade to defaults. */
export function queryFromParams(params: ParamMap): CharacterListQuery {
  const status = (params.get('status') ?? '').toUpperCase();
  const gender = (params.get('gender') ?? '').toUpperCase();
  const page = Number.parseInt(params.get('page') ?? '0', 10);
  return {
    filters: {
      name: params.get('name') ?? '',
      status: isStatus(status) ? status : '',
      species: params.get('species') ?? '',
      gender: isGender(gender) ? gender : '',
    },
    page: Number.isInteger(page) && page > 0 ? page : 0,
  };
}

export function hasActiveFilters(filters: CharacterFilters): boolean {
  return (
    filters.name.trim() !== '' ||
    filters.status !== '' ||
    filters.species.trim() !== '' ||
    filters.gender !== ''
  );
}

export function sameFilters(a: CharacterFilters, b: CharacterFilters): boolean {
  return (
    a.name === b.name && a.status === b.status && a.species === b.species && a.gender === b.gender
  );
}

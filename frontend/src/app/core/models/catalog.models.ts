export type CharacterStatus = 'Alive' | 'Dead' | 'unknown' | string;

export interface CharacterSummary {
  id: number;
  externalId: number;
  name: string;
  status: CharacterStatus;
  species: string;
  type: string | null;
  gender: string;
  imageUrl: string | null;
}

export interface Location {
  id: number;
  externalId: number;
  name: string | null;
  type: string | null;
  dimension: string | null;
}

export interface Episode {
  id: number;
  externalId: number;
  name: string | null;
  airDate: string | null;
  episodeCode: string | null;
}

export interface CharacterDetail {
  character: CharacterSummary;
  origin: Location | null;
  currentLocation: Location | null;
  episodes: Episode[];
}

export interface RelatedCharacter {
  id: number;
  externalId: number;
  name: string;
  sharedEpisodes: number;
}

export interface CharacterFilters {
  name?: string;
  status?: string;
  species?: string;
  type?: string;
  gender?: string;
  page?: number;
  size?: number;
}

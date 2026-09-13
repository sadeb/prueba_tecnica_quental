import { CharacterGender, CharacterStatus } from '../models/character.model';

/**
 * UI vocabulary in Spanish. Only the closed enums of our API are translated; names, species,
 * locations and episode titles are shown exactly as the Rick and Morty API returns them.
 */
export const STATUS_LABELS: Readonly<Record<CharacterStatus, string>> = {
  ALIVE: 'Vivo',
  DEAD: 'Muerto',
  UNKNOWN: 'Desconocido',
};

export const GENDER_LABELS: Readonly<Record<CharacterGender, string>> = {
  FEMALE: 'Femenino',
  MALE: 'Masculino',
  GENDERLESS: 'Sin género',
  UNKNOWN: 'Desconocido',
};

export interface SelectOption<T extends string> {
  readonly value: T;
  readonly label: string;
}

export const STATUS_OPTIONS: readonly SelectOption<CharacterStatus>[] = [
  { value: 'ALIVE', label: STATUS_LABELS.ALIVE },
  { value: 'DEAD', label: STATUS_LABELS.DEAD },
  { value: 'UNKNOWN', label: STATUS_LABELS.UNKNOWN },
];

export const GENDER_OPTIONS: readonly SelectOption<CharacterGender>[] = [
  { value: 'FEMALE', label: GENDER_LABELS.FEMALE },
  { value: 'MALE', label: GENDER_LABELS.MALE },
  { value: 'GENDERLESS', label: GENDER_LABELS.GENDERLESS },
  { value: 'UNKNOWN', label: GENDER_LABELS.UNKNOWN },
];

export function statusLabel(status: CharacterStatus): string {
  return STATUS_LABELS[status] ?? STATUS_LABELS.UNKNOWN;
}

export function genderLabel(gender: CharacterGender): string {
  return GENDER_LABELS[gender] ?? GENDER_LABELS.UNKNOWN;
}

export function pluralize(count: number, singular: string, plural: string): string {
  return `${count} ${count === 1 ? singular : plural}`;
}

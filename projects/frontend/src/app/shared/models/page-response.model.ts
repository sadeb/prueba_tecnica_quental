export interface PageResponse<T> {
  readonly content: readonly T[];
  /** 0-based. */
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
}

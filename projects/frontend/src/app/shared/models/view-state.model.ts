import { ApiError } from '../../core/errors/api-error.model';

/** Every view is always in exactly one of these states; templates render all four. */
export type ViewState<T> =
  | { readonly status: 'loading' }
  | { readonly status: 'empty' }
  | { readonly status: 'error'; readonly error: ApiError }
  | { readonly status: 'ready'; readonly data: T };

export const viewState = {
  loading: <T>(): ViewState<T> => ({ status: 'loading' }),
  empty: <T>(): ViewState<T> => ({ status: 'empty' }),
  error: <T>(error: ApiError): ViewState<T> => ({ status: 'error', error }),
  ready: <T>(data: T): ViewState<T> => ({ status: 'ready', data }),
} as const;

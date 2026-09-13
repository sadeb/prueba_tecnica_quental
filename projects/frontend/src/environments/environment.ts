/**
 * Single environment for every build: `/api` is relative on purpose. In `ng serve` it is
 * resolved by `proxy.conf.json`; in Docker nginx proxies it to the backend. No CORS anywhere.
 */
export const environment = {
  apiBaseUrl: '/api',
  requestTimeoutMs: 15_000,
  transientRetryDelayMs: 750,
  pageSize: 20,
  relatedLimit: 8,
} as const;

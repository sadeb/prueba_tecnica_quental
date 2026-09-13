import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, ErrorHandler, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, TitleStrategy, withInMemoryScrolling } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { errorInterceptor } from './core/errors/error.interceptor';
import { GlobalErrorHandler } from './core/errors/global-error.handler';
import { retryInterceptor } from './core/errors/retry.interceptor';
import { AppTitleStrategy } from './core/routing/app-title.strategy';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }),
    ),
    // Request order: auth → retry → error. Responses flow back in reverse, so `retryInterceptor`
    // already receives an `ApiError` and can decide on `transient`.
    provideHttpClient(withInterceptors([authInterceptor, retryInterceptor, errorInterceptor])),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    { provide: TitleStrategy, useClass: AppTitleStrategy },
  ],
};

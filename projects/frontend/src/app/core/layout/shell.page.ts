import { Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { FavoriteService } from '../../features/favorites/favorite.service';
import { ToastContainerComponent } from '../../shared/ui/toast-container.component';
import { ToastService } from '../notifications/toast.service';
import { NavbarComponent } from './navbar.component';

/** Authenticated layout: navbar, content and toasts. Preloads the favorite ids once per session. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, NavbarComponent, ToastContainerComponent],
  template: `
    <a class="skip-link visually-hidden-focusable" href="#main">Saltar al contenido</a>
    <app-navbar />
    <main id="main" class="container py-4 py-lg-5 app-main" tabindex="-1">
      <router-outlet />
    </main>
    <footer class="container py-4 text-center text-body-secondary small">
      Datos de <span class="fst-italic">Rick and Morty</span> sincronizados por el backend propio · Prueba técnica Quental
    </footer>
    <app-toast-container />
  `,
})
export class ShellPage {
  private readonly favorites = inject(FavoriteService);
  private readonly toasts = inject(ToastService);

  constructor() {
    this.favorites
      .load()
      .pipe(takeUntilDestroyed())
      .subscribe({
        error: () =>
          this.toasts.warning(
            'No se han podido cargar tus favoritos. Los corazones pueden no reflejar tu lista hasta que recargues.',
          ),
      });
  }
}

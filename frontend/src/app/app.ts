import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthApi } from './core/api/auth-api';
import { AuthStore } from './core/auth/auth-store';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.css'
})
export class App {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  protected readonly authStore = inject(AuthStore);

  constructor() {
    effect(() => {
      if (this.authStore.isAuthenticated() || this.router.url.startsWith('/login')) return;
      const returnUrl = this.router.url === '/' ? undefined : this.router.url;
      void this.router.navigate(['/login'], { queryParams: returnUrl ? { returnUrl } : {} });
    });
  }

  protected logout(): void {
    this.authApi.logout().subscribe({
      next: () => void this.router.navigate(['/login']),
      error: () => void this.router.navigate(['/login']),
    });
  }
}

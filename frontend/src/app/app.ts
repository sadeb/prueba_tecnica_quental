import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
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

  protected logout(): void {
    this.authApi.logout().subscribe({
      next: () => void this.router.navigate(['/characters']),
      error: () => void this.router.navigate(['/characters']),
    });
  }
}

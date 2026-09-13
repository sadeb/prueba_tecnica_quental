import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { FavoriteService } from '../../features/favorites/favorite.service';
import { ThemeService } from '../theme/theme.service';
import { ThemeToggleComponent } from './theme-toggle.component';

/** Top navigation. The collapse is driven by a signal: Bootstrap's JS bundle is not needed. */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  private readonly router = inject(Router);

  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
  protected readonly favorites = inject(FavoriteService);
  protected readonly menuOpen = signal(false);

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected logout(): void {
    this.closeMenu();
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}

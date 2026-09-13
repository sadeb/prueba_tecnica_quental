import { Routes } from '@angular/router';
import { authChildGuard, authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage),
    title: 'Iniciar sesión',
  },
  {
    path: '',
    canActivate: [authGuard],
    canActivateChild: [authChildGuard],
    loadComponent: () => import('./core/layout/shell.page').then((m) => m.ShellPage),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'characters' },
      {
        path: 'characters',
        loadComponent: () =>
          import('./features/characters/character-list.page').then((m) => m.CharacterListPage),
        title: 'Personajes',
      },
      {
        path: 'characters/:id',
        loadComponent: () =>
          import('./features/characters/character-detail.page').then((m) => m.CharacterDetailPage),
        title: 'Detalle del personaje',
      },
      {
        path: 'favorites',
        loadComponent: () => import('./features/favorites/favorites.page').then((m) => m.FavoritesPage),
        title: 'Mis favoritos',
      },
      {
        path: '**',
        loadComponent: () => import('./features/not-found/not-found.page').then((m) => m.NotFoundPage),
        title: 'Página no encontrada',
      },
    ],
  },
];

import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'characters' },
  {
    path: 'characters',
    loadComponent: () =>
      import('./features/characters/character-list-page').then((module) => module.CharacterListPage),
  },
  {
    path: 'characters/:id',
    loadComponent: () =>
      import('./features/characters/character-detail-page').then((module) => module.CharacterDetailPage),
  },
  {
    path: 'favorites',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/favorites/favorites-page').then((module) => module.FavoritesPage),
  },
  {
    path: 'admin/sync',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/sync-page').then((module) => module.SyncPage),
  },
  {
    path: 'login',
    data: { mode: 'login' },
    loadComponent: () => import('./features/auth/auth-page').then((module) => module.AuthPage),
  },
  {
    path: 'register',
    data: { mode: 'register' },
    loadComponent: () => import('./features/auth/auth-page').then((module) => module.AuthPage),
  },
  {
    path: '**',
    loadComponent: () => import('./features/not-found/not-found-page').then((module) => module.NotFoundPage),
  },
];

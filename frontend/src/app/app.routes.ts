import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/auth-page').then((module) => module.AuthPage),
  },
  {
    path: '',
    canActivateChild: [authGuard],
    children: [
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
        loadComponent: () =>
          import('./features/favorites/favorites-page').then((module) => module.FavoritesPage),
      },
      {
        path: 'admin/users',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/user-create-page').then((module) => module.UserCreatePage),
      },
      {
        path: 'admin/sync',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/sync-page').then((module) => module.SyncPage),
      },
      {
        path: '**',
        loadComponent: () => import('./features/not-found/not-found-page').then((module) => module.NotFoundPage),
      },
    ],
  },
];

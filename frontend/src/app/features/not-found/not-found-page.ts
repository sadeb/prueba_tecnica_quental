import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink],
  template: `<main class="not-found"><p class="kicker">ERROR 404 / COORDENADAS INVÁLIDAS</p><h1>Esta dimensión no figura en el mapa.</h1><a class="primary-action" routerLink="/characters">Volver al archivo</a></main>`,
  styles: `.not-found{min-height:calc(100vh - 9rem);display:grid;place-content:center;justify-items:start;padding:2rem;background:var(--color-navy);color:white}.not-found h1{max-width:11ch;margin:.8rem 0 2rem;font:780 clamp(3.5rem,9vw,8rem)/.85 var(--font-display);letter-spacing:-.065em}`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundPage {}

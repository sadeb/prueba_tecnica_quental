import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink],
  template: `<main class="not-found">
    <p class="kicker">ERROR 404 / COORDENADAS INVÁLIDAS</p>
    <h1>Esta dimensión no figura en el mapa.</h1>
    <a class="primary-action" routerLink="/characters">Volver al archivo</a>
  </main>`,
  styles: `
    .not-found {
      min-block-size: 100%;
      display: grid;
      place-content: center;
      justify-items: start;
      padding: clamp(1rem, 6vmin, 4rem);
      background: var(--color-navy);
      color: white;
    }
    .not-found h1 {
      max-width: 11ch;
      margin: 0.8rem 0 2rem;
      font: 780 clamp(3rem, 12vmin, 8rem)/0.85 var(--font-display);
      letter-spacing: -0.065em;
      overflow-wrap: anywhere;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundPage {}

import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink, EmptyStateComponent],
  template: `
    <app-empty-state
      title="Esta dimensión no existe"
      message="La dirección que has escrito no corresponde a ninguna página del portal (error 404)."
    >
      <a routerLink="/characters" class="btn btn-primary">Volver a personajes</a>
    </app-empty-state>
  `,
})
export class NotFoundPage {}

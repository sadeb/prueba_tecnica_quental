import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CharacterSummary } from '../../core/models/catalog.models';

@Component({
  selector: 'app-character-card',
  imports: [RouterLink],
  templateUrl: './character-card.html',
  styleUrl: './character-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CharacterCard {
  readonly character = input.required<CharacterSummary>();
  readonly action = input<'add' | 'remove' | null>(null);
  readonly actionRequested = output<CharacterSummary>();

  protected statusClass(status: string): string {
    return status.toLowerCase() === 'alive'
      ? 'status-alive'
      : status.toLowerCase() === 'dead'
        ? 'status-dead'
        : 'status-unknown';
  }
}

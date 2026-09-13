import { Component, input, linkedSignal } from '@angular/core';

/** Character portrait with a graceful fallback when the URL is missing or fails to load. */
@Component({
  selector: 'app-character-image',
  host: { class: 'd-block ratio ratio-1x1 character-image' },
  template: `
    @if (src() && !failed()) {
      <img
        [src]="src()"
        [alt]="alt()"
        loading="lazy"
        decoding="async"
        class="object-fit-cover"
        (error)="failed.set(true)"
      />
    } @else {
      <div class="character-image-fallback d-flex align-items-center justify-content-center" role="img" [attr.aria-label]="alt()">
        <span class="character-image-initial" aria-hidden="true">{{ initial() }}</span>
      </div>
    }
  `,
})
export class CharacterImageComponent {
  readonly src = input<string | null>(null);
  readonly alt = input.required<string>();

  /** Resets automatically when `src` changes. */
  protected readonly failed = linkedSignal<string | null, boolean>({
    source: this.src,
    computation: () => false,
  });

  protected initial(): string {
    return this.alt().trim().charAt(0).toUpperCase() || '?';
  }
}

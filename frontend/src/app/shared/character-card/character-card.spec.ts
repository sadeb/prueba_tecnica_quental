import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CharacterSummary } from '../../core/models/catalog.models';
import { CharacterCard } from './character-card';

const character: CharacterSummary = {
  id: 1,
  externalId: 1,
  name: 'Rick Sanchez',
  status: 'Alive',
  species: 'Human',
  type: null,
  gender: 'Male',
  imageUrl: null,
};

describe('CharacterCard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CharacterCard],
      providers: [provideRouter([])],
    });
  });

  it('presents the heart as available when the character is not a favorite', async () => {
    const fixture = TestBed.createComponent(CharacterCard);
    fixture.componentRef.setInput('character', character);

    await fixture.whenStable();

    const button = fixture.nativeElement.querySelector('.favorite-button') as HTMLButtonElement;
    expect(button.classList).not.toContain('is-favorite');
    expect(button.getAttribute('aria-pressed')).toBe('false');
    expect(button.getAttribute('aria-label')).toContain('Guardar a Rick Sanchez');
  });

  it('presents the heart as selected and emits the character when clicked', async () => {
    const fixture = TestBed.createComponent(CharacterCard);
    fixture.componentRef.setInput('character', character);
    fixture.componentRef.setInput('favorite', true);
    let emitted: CharacterSummary | undefined;
    fixture.componentInstance.favoriteToggled.subscribe((value) => (emitted = value));

    await fixture.whenStable();
    const button = fixture.nativeElement.querySelector('.favorite-button') as HTMLButtonElement;
    button.click();

    expect(button.classList).toContain('is-favorite');
    expect(button.getAttribute('aria-pressed')).toBe('true');
    expect(button.getAttribute('aria-label')).toContain('Retirar a Rick Sanchez');
    expect(emitted).toEqual(character);
  });
});

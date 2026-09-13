import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CharacterFilters, EMPTY_FILTERS } from '../../shared/models/character.model';
import { CharacterFiltersComponent } from './character-filters.component';

describe('CharacterFiltersComponent', () => {
  let fixture: ComponentFixture<CharacterFiltersComponent>;
  let emitted: CharacterFilters[];

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({ imports: [CharacterFiltersComponent] }).compileComponents();
    fixture = TestBed.createComponent(CharacterFiltersComponent);
    fixture.componentRef.setInput('filters', EMPTY_FILTERS);
    emitted = [];
    fixture.componentInstance.filtersChange.subscribe((value) => emitted.push(value));
    fixture.detectChanges();
  });

  afterEach(() => vi.useRealTimers());

  function element<T extends HTMLElement>(selector: string): T {
    const found = (fixture.nativeElement as HTMLElement).querySelector<T>(selector);
    if (found === null) {
      throw new Error(`Missing ${selector}`);
    }
    return found;
  }

  it('does not emit on init when the model equals the input', () => {
    expect(emitted).toEqual([]);
  });

  it('debounces the name field before emitting', async () => {
    const name = element<HTMLInputElement>('#filter-name');
    name.value = 'ri';
    name.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await vi.advanceTimersByTimeAsync(100);
    fixture.detectChanges();
    expect(emitted).toEqual([]);

    // The debouncer resolves a promise after the timeout: flush timers and microtasks.
    await vi.advanceTimersByTimeAsync(300);
    fixture.detectChanges();
    expect(emitted).toEqual([{ ...EMPTY_FILTERS, name: 'ri' }]);
  });

  it('emits immediately when a select changes', () => {
    const status = element<HTMLSelectElement>('#filter-status');
    status.value = 'DEAD';
    status.dispatchEvent(new Event('change'));
    status.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(emitted).toEqual([{ ...EMPTY_FILTERS, status: 'DEAD' }]);
  });

  it('follows the input when the parent (query params) changes and does not echo it back', () => {
    fixture.componentRef.setInput('filters', { ...EMPTY_FILTERS, name: 'morty' });
    fixture.detectChanges();
    expect(element<HTMLInputElement>('#filter-name').value).toBe('morty');
    expect(emitted).toEqual([]);
  });

  it('reset emits the empty filters and the button is disabled when nothing is active', () => {
    const button = element<HTMLButtonElement>('button.btn-outline-secondary');
    expect(button.disabled).toBe(true);
    fixture.componentRef.setInput('filters', { ...EMPTY_FILTERS, status: 'ALIVE' });
    fixture.detectChanges();
    expect(button.disabled).toBe(false);
    button.click();
    fixture.detectChanges();
    expect(emitted).toEqual([EMPTY_FILTERS]);
  });
});

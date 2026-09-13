import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, ParamMap, provideRouter, Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { aCharacter, anApiError, aPage } from '../../../testing/test-data';
import { ToastService } from '../../core/notifications/toast.service';
import { FavoriteService } from '../favorites/favorite.service';
import { CharacterListPage } from './character-list.page';
import { CharacterService } from './character.service';

describe('CharacterListPage', () => {
  let fixture: ComponentFixture<CharacterListPage>;
  let queryParams: BehaviorSubject<ParamMap>;
  let router: Router;
  const characters = { search: vi.fn() };
  const favorites = {
    isFavorite: vi.fn(() => false),
    isPending: vi.fn(() => false),
    toggle: vi.fn(),
  };
  const toasts = { success: vi.fn(), error: vi.fn() };

  function html(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  beforeEach(async () => {
    characters.search.mockReset();
    favorites.toggle.mockReset();
    toasts.success.mockReset();
    toasts.error.mockReset();
    queryParams = new BehaviorSubject<ParamMap>(convertToParamMap({}));
    await TestBed.configureTestingModule({
      imports: [CharacterListPage],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParamMap: queryParams.asObservable() } },
        { provide: CharacterService, useValue: characters },
        { provide: FavoriteService, useValue: favorites },
        { provide: ToastService, useValue: toasts },
      ],
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  function create(): void {
    fixture = TestBed.createComponent(CharacterListPage);
    fixture.detectChanges();
  }

  it('renders the ready state with one card per character', () => {
    characters.search.mockReturnValue(of(aPage([aCharacter({ id: 1 }), aCharacter({ id: 2, name: 'Morty Smith' })])));
    create();
    expect(characters.search).toHaveBeenCalledWith({ name: '', status: '', species: '', gender: '' }, 0);
    expect(fixture.nativeElement.querySelectorAll('app-character-card').length).toBe(2);
    expect(html()).toContain('2 personajes encontrados');
  });

  it('renders the empty state when the page has no content', () => {
    characters.search.mockReturnValue(of(aPage([])));
    create();
    expect(html()).toContain('Ningún personaje coincide');
  });

  it('renders the error alert with the Spanish message and retries on demand', () => {
    characters.search
      .mockReturnValueOnce(throwError(() => anApiError({ status: 503, code: 'SERVICE_UNAVAILABLE', userMessage: 'Caído.' })))
      .mockReturnValueOnce(of(aPage([aCharacter()])));
    create();
    expect(html()).toContain('No se ha podido cargar la lista de personajes');
    expect(html()).toContain('Caído.');

    (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('app-error-alert button')?.click();
    fixture.detectChanges();
    expect(characters.search).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.querySelectorAll('app-character-card').length).toBe(1);
  });

  it('shows the loading skeletons while the request is in flight', () => {
    characters.search.mockReturnValue(new BehaviorSubject(aPage([])).asObservable().pipe());
    characters.search.mockReturnValue(of()); // never emits
    create();
    expect(fixture.nativeElement.querySelectorAll('app-skeleton-card').length).toBeGreaterThan(0);
  });

  it('reads filters and page from the query params', () => {
    characters.search.mockReturnValue(of(aPage([aCharacter()])));
    queryParams.next(convertToParamMap({ name: 'rick', status: 'alive', page: '2' }));
    create();
    expect(characters.search).toHaveBeenCalledWith({ name: 'rick', status: 'ALIVE', species: '', gender: '' }, 2);
  });

  it('changing a filter writes it to the url and resets the page', () => {
    characters.search.mockReturnValue(of(aPage([aCharacter()])));
    queryParams.next(convertToParamMap({ page: '3' }));
    create();
    (fixture.componentInstance as unknown as { onFiltersChange: (f: unknown) => void }).onFiltersChange({
      name: ' summer ',
      status: 'DEAD',
      species: '',
      gender: '',
    });
    expect(router.navigate).toHaveBeenCalledWith([], {
      relativeTo: expect.anything(),
      queryParams: { name: 'summer', status: 'DEAD', species: null, gender: null, page: null },
      queryParamsHandling: 'merge',
    });
  });

  it('changing the page keeps the filters (merge) and drops page=0', () => {
    characters.search.mockReturnValue(of(aPage([aCharacter()])));
    create();
    const page = fixture.componentInstance as unknown as { onPageChange: (p: number) => void };
    page.onPageChange(2);
    expect(router.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({ queryParams: { page: 2 } }));
    page.onPageChange(0);
    expect(router.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({ queryParams: { page: null } }));
  });

  it('toggling a favorite shows a toast on success and the error message on failure', () => {
    characters.search.mockReturnValue(of(aPage([aCharacter({ id: 1, name: 'Rick Sanchez' })])));
    favorites.toggle.mockReturnValueOnce(of(aCharacter())).mockReturnValueOnce(
      throwError(() => anApiError({ status: 404, code: 'NOT_FOUND', userMessage: 'x' })),
    );
    create();
    const toggle = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('app-favorite-toggle button');
    toggle?.click();
    expect(toasts.success).toHaveBeenCalledWith('Rick Sanchez añadido a tus favoritos.');
    toggle?.click();
    expect(toasts.error).toHaveBeenCalledWith('Ese personaje no está en tus favoritos o ya no existe.');
  });
});

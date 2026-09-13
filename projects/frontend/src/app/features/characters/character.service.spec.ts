import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { aCharacter, aPage } from '../../../testing/test-data';
import { EMPTY_FILTERS } from '../../shared/models/character.model';
import { CharacterService } from './character.service';

describe('CharacterService', () => {
  let service: CharacterService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CharacterService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('sends only the filters that have a value, plus page and size', () => {
    service.search({ ...EMPTY_FILTERS, name: '  rick ', status: 'ALIVE' }, 2, 10).subscribe();
    const req = controller.expectOne((r) => r.url === '/api/characters');
    expect(req.request.params.keys().sort()).toEqual(['name', 'page', 'size', 'status']);
    expect(req.request.params.get('name')).toBe('rick');
    expect(req.request.params.get('status')).toBe('ALIVE');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('10');
    req.flush(aPage([aCharacter()]));
  });

  it('omits every filter when none is set', () => {
    service.search(EMPTY_FILTERS, 0).subscribe();
    const req = controller.expectOne((r) => r.url === '/api/characters');
    expect(req.request.params.keys().sort()).toEqual(['page', 'size']);
    expect(req.request.params.get('size')).toBe('20');
    req.flush(aPage([]));
  });

  it('builds detail and related urls from the internal id', () => {
    service.getById(7).subscribe();
    controller.expectOne('/api/characters/7').flush(aCharacter({ id: 7 }));
    service.getRelated(7, 5).subscribe();
    const related = controller.expectOne((r) => r.url === '/api/characters/7/related');
    expect(related.request.params.get('limit')).toBe('5');
    related.flush([]);
  });
});

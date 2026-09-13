import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CharacterSummary } from '../models/catalog.models';
import { FavoritesApi } from './favorites-api';

const rick: CharacterSummary = {
  id: 1,
  externalId: 1,
  name: 'Rick Sanchez',
  status: 'Alive',
  species: 'Human',
  type: null,
  gender: 'Male',
  imageUrl: null,
};

const morty: CharacterSummary = {
  ...rick,
  id: 2,
  externalId: 2,
  name: 'Morty Smith',
};

describe('FavoritesApi', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('collects every favorites page to determine card state', () => {
    const api = TestBed.inject(FavoritesApi);
    const http = TestBed.inject(HttpTestingController);
    let favorites: CharacterSummary[] = [];

    api.listAll().subscribe((result) => (favorites = result));

    http.expectOne('/api/v1/users/me/favorites?page=0&size=100').flush({
      content: [rick],
      page: 0,
      size: 100,
      totalElements: 2,
      totalPages: 2,
    });
    http.expectOne('/api/v1/users/me/favorites?page=1&size=100').flush({
      content: [morty],
      page: 1,
      size: 100,
      totalElements: 2,
      totalPages: 2,
    });

    expect(favorites).toEqual([rick, morty]);
    http.verify();
  });
});

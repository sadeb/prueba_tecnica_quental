import { convertToParamMap } from '@angular/router';
import { hasActiveFilters, queryFromParams } from './character.model';

describe('queryFromParams', () => {
  it('reads filters and page from the query string', () => {
    const query = queryFromParams(
      convertToParamMap({ name: 'rick', status: 'alive', species: 'Human', gender: 'MALE', page: '3' }),
    );
    expect(query).toEqual({
      filters: { name: 'rick', status: 'ALIVE', species: 'Human', gender: 'MALE' },
      page: 3,
    });
    expect(hasActiveFilters(query.filters)).toBe(true);
  });

  it('degrades invalid enum values and pages to defaults', () => {
    const query = queryFromParams(convertToParamMap({ status: 'zombie', gender: 'x', page: '-4' }));
    expect(query.filters.status).toBe('');
    expect(query.filters.gender).toBe('');
    expect(query.page).toBe(0);
    expect(queryFromParams(convertToParamMap({ page: 'abc' })).page).toBe(0);
    expect(hasActiveFilters(query.filters)).toBe(false);
  });
});

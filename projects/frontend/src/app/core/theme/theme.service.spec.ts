import { TestBed } from '@angular/core/testing';
import { InMemoryStorage } from '../../../testing/in-memory-storage';
import { BrowserStorage } from '../storage/browser-storage';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let storage: InMemoryStorage;

  function setup(): ThemeService {
    TestBed.configureTestingModule({ providers: [{ provide: BrowserStorage, useValue: storage }] });
    const service = TestBed.inject(ThemeService);
    TestBed.tick();
    return service;
  }

  beforeEach(() => {
    storage = new InMemoryStorage();
    document.documentElement.removeAttribute('data-bs-theme');
  });

  it('defaults to light when nothing is stored and the OS has no preference', () => {
    const service = setup();
    expect(service.theme()).toBe('light');
    expect(document.documentElement.getAttribute('data-bs-theme')).toBe('light');
  });

  it('restores the stored theme', () => {
    storage.set('rm.theme', 'dark');
    const service = setup();
    expect(service.isDark()).toBe(true);
    expect(document.documentElement.getAttribute('data-bs-theme')).toBe('dark');
  });

  it('toggle switches the theme, updates the DOM and persists the choice', () => {
    const service = setup();
    service.toggle();
    TestBed.tick();
    expect(service.theme()).toBe('dark');
    expect(document.documentElement.getAttribute('data-bs-theme')).toBe('dark');
    expect(storage.get('rm.theme')).toBe('dark');
    service.toggle();
    TestBed.tick();
    expect(document.documentElement.getAttribute('data-bs-theme')).toBe('light');
  });

  it('ignores an invalid stored value', () => {
    storage.set('rm.theme', 'blue');
    expect(setup().theme()).toBe('light');
  });
});

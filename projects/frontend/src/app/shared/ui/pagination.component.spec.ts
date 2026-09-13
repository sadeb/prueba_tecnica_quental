import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaginationComponent } from './pagination.component';

describe('PaginationComponent', () => {
  let fixture: ComponentFixture<PaginationComponent>;
  let emitted: number[];

  function render(page: number, totalPages: number): void {
    fixture.componentRef.setInput('page', page);
    fixture.componentRef.setInput('totalPages', totalPages);
    fixture.detectChanges();
  }

  function labels(): string[] {
    return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('li button'))
      .map((button) => button.textContent?.trim() ?? '')
      .filter((label) => /^\d+$/.test(label));
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PaginationComponent] }).compileComponents();
    fixture = TestBed.createComponent(PaginationComponent);
    emitted = [];
    fixture.componentInstance.pageChange.subscribe((page) => emitted.push(page));
  });

  it('renders nothing with a single page', () => {
    render(0, 1);
    expect((fixture.nativeElement as HTMLElement).querySelector('nav')).toBeNull();
  });

  it('shows a window of five pages centred on the current one', () => {
    render(7, 20);
    expect(labels()).toEqual(['6', '7', '8', '9', '10']);
    render(0, 20);
    expect(labels()).toEqual(['1', '2', '3', '4', '5']);
    render(19, 20);
    expect(labels()).toEqual(['16', '17', '18', '19', '20']);
  });

  it('emits 0-based pages and ignores out-of-range or current page clicks', () => {
    render(0, 3);
    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('li button');
    buttons[0].click(); // previous: disabled
    buttons[1].click(); // current page
    buttons[2].click(); // page 2 → index 1
    buttons[buttons.length - 1].click(); // next → index 1
    expect(emitted).toEqual([1, 1]);
  });
});

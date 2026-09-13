import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SyncRun } from '../../core/models/sync.models';
import { SyncPage } from './sync-page';

const manualRun: SyncRun = {
  id: 'manual-run-1',
  status: 'QUEUING',
  trigger: 'MANUAL',
  startedAt: '2026-09-11T19:18:46Z',
  completedAt: null,
  pagesFetched: 0,
  messagesQueued: 0,
  processedCount: 0,
  failedCount: 0,
  errorMessage: null,
};

describe('SyncPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SyncPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('refreshes a manual run every minute until it finishes', fakeAsync(() => {
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(SyncPage);

    http.expectOne('/api/v1/admin/sync-runs?page=0&size=20').flush({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });
    tick();

    const startButton = fixture.nativeElement.querySelector('.primary-action') as HTMLButtonElement;
    startButton.click();
    http.expectOne('/api/v1/admin/sync-runs').flush(manualRun);
    tick();

    tick(59_999);
    http.expectNone('/api/v1/admin/sync-runs/manual-run-1');

    tick(1);
    http.expectOne('/api/v1/admin/sync-runs/manual-run-1').flush({
      ...manualRun,
      status: 'COMPLETED',
      completedAt: '2026-09-11T19:19:46Z',
      pagesFetched: 42,
      messagesQueued: 826,
      processedCount: 826,
    });
    tick();

    expect(fixture.componentInstance['polling']()).toBeFalse();
    http.verify();
  }));
});

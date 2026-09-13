import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { UsersApi } from '../../core/api/users-api';
import { AdminUser } from '../../core/models/auth.models';
import { ToastService } from '../../shared/toast/toast.service';
import { UserManagementPage } from './user-management-page';

describe('UserManagementPage', () => {
  let fixture: ComponentFixture<UserManagementPage>;
  let usersApi: jasmine.SpyObj<UsersApi>;
  let toastService: ToastService;

  const user: AdminUser = {
    id: 7,
    username: 'portal-user',
    role: 'USER',
    enabled: true,
    createdAt: '2026-09-13T12:00:00Z',
  };

  const administrator: AdminUser = {
    id: 1,
    username: 'admin',
    role: 'ADMIN',
    enabled: true,
    createdAt: '2026-09-01T08:00:00Z',
  };

  function element(): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  function buttonByLabel(label: string): HTMLButtonElement | undefined {
    return Array.from(element().querySelectorAll('button')).find(
      (button) => button.getAttribute('aria-label') === label,
    );
  }

  function buttonByText(text: string): HTMLButtonElement | undefined {
    return Array.from(element().querySelectorAll('button')).find(
      (button) => button.textContent?.trim() === text,
    );
  }

  function typeInto(id: string, value: string): void {
    const input = element().querySelector<HTMLInputElement>(`#${id}`);
    if (!input) throw new Error(`Missing input #${id}`);
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('blur', { bubbles: true }));
  }

  beforeEach(() => {
    usersApi = jasmine.createSpyObj<UsersApi>('UsersApi', ['list', 'create', 'update', 'delete']);
    usersApi.list.and.returnValue(
      of({ content: [administrator, user], page: 0, size: 10, totalElements: 2, totalPages: 1 }),
    );
    usersApi.create.and.returnValue(of({ ...user, id: 9, username: 'new.operator' }));
    usersApi.update.and.returnValue(of({ ...user, username: 'renamed' }));
    usersApi.delete.and.returnValue(of(undefined));

    TestBed.configureTestingModule({ providers: [{ provide: UsersApi, useValue: usersApi }] });
    fixture = TestBed.createComponent(UserManagementPage);
    toastService = TestBed.inject(ToastService);
  });

  afterEach(() => {
    toastService.toasts().forEach((toast) => toastService.dismiss(toast.id));
  });

  it('renders the users returned by the API in a sortable datatable', async () => {
    await fixture.whenStable();

    const table = element().querySelector('.users-table') as HTMLTableElement;
    expect(table).toBeTruthy();
    expect(table.textContent).toContain('portal-user');
    expect(table.textContent).toContain('Activa');
    expect(usersApi.list).toHaveBeenCalledWith({
      search: '',
      page: 0,
      size: 10,
      sort: 'username',
      direction: 'asc',
    });

    const usernameHeader = element().querySelector('th[aria-sort="ascending"]');
    expect(usernameHeader?.textContent).toContain('Usuario');
    expect(buttonByLabel('Editar a portal-user')?.disabled).toBeFalse();
    expect(buttonByLabel('La cuenta administradora no se puede eliminar')?.disabled).toBeTrue();
  });

  it('toggles the sort direction when the active column header is clicked', async () => {
    await fixture.whenStable();
    usersApi.list.calls.reset();

    buttonByText('Usuario')?.click();
    await fixture.whenStable();

    expect(usersApi.list).toHaveBeenCalledWith(
      jasmine.objectContaining({ sort: 'username', direction: 'desc', page: 0 }),
    );
  });

  it('creates a user from the dialog and notifies through a toast', async () => {
    await fixture.whenStable();

    buttonByText('Nuevo usuario')?.click();
    await fixture.whenStable();

    const dialog = element().querySelector<HTMLDialogElement>('dialog.app-dialog:not(.danger)');
    expect(dialog?.open).toBeTrue();

    typeInto('dialog-username', 'new.operator');
    typeInto('dialog-password', 'secret-123');
    await fixture.whenStable();
    buttonByText('Crear usuario')?.click();
    await fixture.whenStable();

    expect(usersApi.create).toHaveBeenCalledOnceWith({
      username: 'new.operator',
      password: 'secret-123',
    });
    expect(dialog?.open).toBeFalse();
    expect(toastService.toasts().some((toast) => toast.tone === 'success')).toBeTrue();
  });

  it('requires explicit confirmation in a dialog before deleting a user', async () => {
    await fixture.whenStable();

    buttonByLabel('Eliminar a portal-user')?.click();
    await fixture.whenStable();

    expect(usersApi.delete).not.toHaveBeenCalled();
    const dialog = element().querySelector<HTMLDialogElement>('dialog.app-dialog.danger');
    expect(dialog?.open).toBeTrue();

    buttonByText('Eliminar definitivamente')?.click();
    await fixture.whenStable();

    expect(usersApi.delete).toHaveBeenCalledOnceWith(7);
    expect(dialog?.open).toBeFalse();
    expect(toastService.toasts().some((toast) => toast.tone === 'success')).toBeTrue();
  });
});

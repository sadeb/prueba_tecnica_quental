import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormField,
  form,
  maxLength,
  minLength,
  pattern,
  required,
  submit,
  validate,
} from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { UsersApi } from '../../core/api/users-api';
import { apiErrorMessage } from '../../core/http/api-error-message';
import {
  AdminUser,
  SortDirection,
  UpdateUserRequest,
  UserSortField,
} from '../../core/models/auth.models';
import { AppIcon } from '../../shared/icon/app-icon';
import { ToastService } from '../../shared/toast/toast.service';

type DialogMode = 'create' | 'edit';
type SortableColumn = Exclude<UserSortField, 'id'>;

export const USER_PAGE_SIZES = [10, 25, 50] as const;
const SEARCH_DEBOUNCE_MS = 350;
const MAX_VISIBLE_PAGES = 5;

@Component({
  selector: 'app-user-management-page',
  imports: [DatePipe, FormField, AppIcon],
  templateUrl: './user-management-page.html',
  styleUrl: './user-management-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserManagementPage {
  private readonly usersApi = inject(UsersApi);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly userDialog = viewChild.required<ElementRef<HTMLDialogElement>>('userDialog');
  private readonly deleteDialog = viewChild.required<ElementRef<HTMLDialogElement>>('deleteDialog');
  private searchTimer: ReturnType<typeof setTimeout> | null = null;

  protected readonly pageSizes = USER_PAGE_SIZES;
  protected readonly users = signal<AdminUser[]>([]);
  protected readonly loading = signal(true);
  protected readonly listError = signal('');
  protected readonly search = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal<number>(USER_PAGE_SIZES[0]);
  protected readonly sortField = signal<SortableColumn>('username');
  protected readonly sortDirection = signal<SortDirection>('asc');
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);

  protected readonly dialogMode = signal<DialogMode>('create');
  protected readonly selectedUser = signal<AdminUser | null>(null);
  protected readonly deleteCandidate = signal<AdminUser | null>(null);
  protected readonly saving = signal(false);
  protected readonly deleting = signal(false);
  protected readonly dialogError = signal('');

  protected readonly hasUsers = computed(() => this.users().length > 0);
  protected readonly canGoBack = computed(() => this.page() > 0);
  protected readonly canGoForward = computed(() => this.page() + 1 < this.totalPages());
  protected readonly rangeStart = computed(() =>
    this.totalElements() === 0 ? 0 : this.page() * this.pageSize() + 1,
  );
  protected readonly rangeEnd = computed(() =>
    Math.min(this.totalElements(), this.page() * this.pageSize() + this.users().length),
  );
  protected readonly visiblePages = computed<number[]>(() => {
    const total = this.totalPages();
    if (total <= 0) return [];
    const half = Math.floor(MAX_VISIBLE_PAGES / 2);
    let start = Math.max(0, this.page() - half);
    const end = Math.min(total - 1, start + MAX_VISIBLE_PAGES - 1);
    start = Math.max(0, end - MAX_VISIBLE_PAGES + 1);
    return Array.from({ length: end - start + 1 }, (_, index) => start + index);
  });

  protected readonly userModel = signal({ username: '', password: '', enabled: true });
  protected readonly userForm = form(this.userModel, (path) => {
    required(path.username, { message: 'Escribe un nombre de usuario.' });
    minLength(path.username, 3, { message: 'Usa al menos 3 caracteres.' });
    maxLength(path.username, 80, { message: 'Usa como máximo 80 caracteres.' });
    pattern(path.username, /^[A-Za-z0-9._-]+$/, {
      message: 'Usa solo letras, números, punto, guion o guion bajo.',
    });
    maxLength(path.password, 72, { message: 'Usa como máximo 72 caracteres.' });
    validate(path.password, ({ value }) => {
      const password = value();
      if (this.dialogMode() === 'create' && password.length === 0) {
        return { kind: 'required', message: 'Escribe una contraseña inicial.' };
      }
      if (password.length > 0 && password.length < 8) {
        return { kind: 'minLength', message: 'Usa al menos 8 caracteres.' };
      }
      return undefined;
    });
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.clearSearchTimer());
    this.load();
  }

  protected load(targetPage = this.page()): void {
    this.loading.set(true);
    this.listError.set('');
    this.usersApi
      .list({
        search: this.search(),
        page: targetPage,
        size: this.pageSize(),
        sort: this.sortField(),
        direction: this.sortDirection(),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.users.set(result.content);
          this.page.set(result.page);
          this.totalElements.set(result.totalElements);
          this.totalPages.set(result.totalPages);
          this.loading.set(false);
        },
        error: (error) => {
          this.listError.set(apiErrorMessage(error, 'No fue posible consultar los usuarios.'));
          this.loading.set(false);
        },
      });
  }

  protected onSearchInput(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
    this.clearSearchTimer();
    this.searchTimer = setTimeout(() => {
      this.searchTimer = null;
      this.load(0);
    }, SEARCH_DEBOUNCE_MS);
  }

  protected submitSearch(event: SubmitEvent): void {
    event.preventDefault();
    this.clearSearchTimer();
    this.load(0);
  }

  protected clearSearch(): void {
    this.clearSearchTimer();
    this.search.set('');
    this.load(0);
  }

  protected changePageSize(event: Event): void {
    const size = Number((event.target as HTMLSelectElement).value);
    if (!Number.isFinite(size) || size <= 0) return;
    this.pageSize.set(size);
    this.load(0);
  }

  protected sortBy(column: SortableColumn): void {
    if (this.sortField() === column) {
      this.sortDirection.update((direction) => (direction === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortField.set(column);
      this.sortDirection.set('asc');
    }
    this.load(0);
  }

  protected ariaSort(column: SortableColumn): 'ascending' | 'descending' | 'none' {
    if (this.sortField() !== column) return 'none';
    return this.sortDirection() === 'asc' ? 'ascending' : 'descending';
  }

  protected sortIcon(column: SortableColumn): 'sort' | 'sort-asc' | 'sort-desc' {
    if (this.sortField() !== column) return 'sort';
    return this.sortDirection() === 'asc' ? 'sort-asc' : 'sort-desc';
  }

  protected isProtected(user: AdminUser): boolean {
    return user.role === 'ADMIN';
  }

  protected openCreate(): void {
    this.dialogMode.set('create');
    this.selectedUser.set(null);
    this.resetUserForm({ username: '', password: '', enabled: true });
    this.showDialog(this.userDialog());
  }

  protected openEdit(user: AdminUser): void {
    if (this.isProtected(user)) return;
    this.dialogMode.set('edit');
    this.selectedUser.set(user);
    this.resetUserForm({ username: user.username, password: '', enabled: user.enabled });
    this.showDialog(this.userDialog());
  }

  protected closeUserDialog(): void {
    if (this.saving()) return;
    this.closeDialog(this.userDialog());
  }

  protected onUserDialogClosed(): void {
    this.selectedUser.set(null);
    this.dialogError.set('');
  }

  protected saveUser(event: SubmitEvent): void {
    event.preventDefault();
    void submit(this.userForm, async () => {
      if (this.saving()) return;
      this.saving.set(true);
      this.dialogError.set('');
      try {
        if (this.dialogMode() === 'create') {
          await this.createUser();
        } else {
          await this.updateUser();
        }
        this.closeDialog(this.userDialog());
      } catch (error) {
        const fallback =
          this.dialogMode() === 'create'
            ? 'No fue posible crear la cuenta.'
            : 'No fue posible actualizar la cuenta.';
        this.dialogError.set(apiErrorMessage(error, fallback));
      } finally {
        this.saving.set(false);
      }
    });
  }

  protected requestDelete(user: AdminUser): void {
    if (this.isProtected(user)) return;
    this.deleteCandidate.set(user);
    this.showDialog(this.deleteDialog());
  }

  protected closeDeleteDialog(): void {
    if (this.deleting()) return;
    this.closeDialog(this.deleteDialog());
  }

  protected onDeleteDialogClosed(): void {
    this.deleteCandidate.set(null);
  }

  protected async confirmDelete(): Promise<void> {
    const user = this.deleteCandidate();
    if (!user || this.deleting()) return;
    this.deleting.set(true);
    try {
      await firstValueFrom(this.usersApi.delete(user.id).pipe(takeUntilDestroyed(this.destroyRef)));
      this.closeDialog(this.deleteDialog());
      this.toast.success(`La cuenta de ${user.username} fue eliminada.`, 'Cuenta eliminada');
      const previousPage =
        this.users().length === 1 && this.page() > 0 ? this.page() - 1 : this.page();
      this.load(previousPage);
    } catch (error) {
      this.toast.error(apiErrorMessage(error, 'No fue posible eliminar la cuenta.'));
    } finally {
      this.deleting.set(false);
    }
  }

  protected fieldError(field: 'username' | 'password'): string {
    return this.userForm[field]().errors()[0]?.message?.toString() ?? '';
  }

  private async createUser(): Promise<void> {
    const model = this.userModel();
    const user = await firstValueFrom(
      this.usersApi
        .create({ username: model.username, password: model.password })
        .pipe(takeUntilDestroyed(this.destroyRef)),
    );
    this.toast.success(`La cuenta de ${user.username} quedó creada.`, 'Usuario creado');
    this.load(0);
  }

  private async updateUser(): Promise<void> {
    const selected = this.selectedUser();
    if (!selected) return;
    const model = this.userModel();
    const request: UpdateUserRequest = {
      username: model.username,
      enabled: model.enabled,
      ...(model.password ? { password: model.password } : {}),
    };
    const updated = await firstValueFrom(
      this.usersApi.update(selected.id, request).pipe(takeUntilDestroyed(this.destroyRef)),
    );
    this.toast.success(`Los cambios de ${updated.username} quedaron guardados.`, 'Usuario actualizado');
    this.load(this.page());
  }

  private resetUserForm(model: { username: string; password: string; enabled: boolean }): void {
    this.userModel.set(model);
    this.userForm().reset();
    this.dialogError.set('');
  }

  private showDialog(dialog: ElementRef<HTMLDialogElement>): void {
    const element = dialog.nativeElement;
    if (element.open) return;
    if (typeof element.showModal === 'function') {
      element.showModal();
    } else {
      element.setAttribute('open', '');
    }
  }

  private closeDialog(dialog: ElementRef<HTMLDialogElement>): void {
    const element = dialog.nativeElement;
    if (!element.open) return;
    if (typeof element.close === 'function') {
      element.close();
    } else {
      element.removeAttribute('open');
      element.dispatchEvent(new Event('close'));
    }
  }

  protected onBackdropClick(event: MouseEvent, dialog: 'user' | 'delete'): void {
    if (event.target !== event.currentTarget) return;
    if (dialog === 'user') {
      this.closeUserDialog();
    } else {
      this.closeDeleteDialog();
    }
  }

  private clearSearchTimer(): void {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
      this.searchTimer = null;
    }
  }
}

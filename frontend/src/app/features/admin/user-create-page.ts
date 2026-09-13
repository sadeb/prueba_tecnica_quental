import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormField, form, maxLength, minLength, pattern, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { UsersApi } from '../../core/api/users-api';
import { apiErrorMessage } from '../../core/http/api-error-message';

@Component({
  selector: 'app-user-create-page',
  imports: [FormField],
  templateUrl: './user-create-page.html',
  styleUrl: './user-create-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserCreatePage {
  private readonly usersApi = inject(UsersApi);

  protected readonly pending = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly newUser = signal({ username: '', password: '' });
  protected readonly newUserForm = form(this.newUser, (path) => {
    required(path.username, { message: 'Escribe un nombre de usuario.' });
    minLength(path.username, 3, { message: 'Usa al menos 3 caracteres.' });
    maxLength(path.username, 80, { message: 'Usa como máximo 80 caracteres.' });
    pattern(path.username, /^[A-Za-z0-9._-]+$/, {
      message: 'Usa solo letras, números, punto, guion o guion bajo.',
    });
    required(path.password, { message: 'Escribe una contraseña.' });
    minLength(path.password, 8, { message: 'Usa al menos 8 caracteres.' });
    maxLength(path.password, 72, { message: 'Usa como máximo 72 caracteres.' });
  });

  protected createUser(event: SubmitEvent): void {
    event.preventDefault();
    void submit(this.newUserForm, async () => {
      if (this.pending()) return;
      this.pending.set(true);
      this.errorMessage.set('');
      this.successMessage.set('');
      try {
        const user = await firstValueFrom(this.usersApi.create(this.newUser()));
        this.successMessage.set(`La cuenta de ${user.username} quedó creada con acceso de usuario.`);
        this.newUserForm().reset();
      } catch (error) {
        this.errorMessage.set(apiErrorMessage(error, 'No fue posible crear la cuenta.'));
      } finally {
        this.pending.set(false);
      }
    });
  }

  protected fieldError(field: 'username' | 'password'): string {
    const errors = this.newUserForm[field]().errors();
    return errors[0]?.message?.toString() ?? '';
  }
}

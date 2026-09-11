import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormField, form, minLength, required } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthApi } from '../../core/api/auth-api';
import { apiErrorMessage } from '../../core/http/api-error-message';

@Component({
  selector: 'app-auth-page',
  imports: [FormField, RouterLink],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthPage {
  private readonly authApi = inject(AuthApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isRegister = this.route.snapshot.data['mode'] === 'register';
  protected readonly pending = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly credentials = signal({ username: '', password: '' });
  protected readonly credentialsForm = form(this.credentials, (path) => {
    required(path.username, { message: 'Escribe un nombre de usuario.' });
    minLength(path.username, 3, { message: 'Usa al menos 3 caracteres.' });
    required(path.password, { message: 'Escribe una contraseña.' });
    minLength(path.password, 8, { message: 'Usa al menos 8 caracteres.' });
  });

  protected submit(event: SubmitEvent): void {
    event.preventDefault();
    this.credentialsForm().markAsTouched();
    if (this.credentialsForm().invalid() || this.pending()) return;

    this.pending.set(true);
    this.errorMessage.set('');
    const request = this.credentials();
    const operation = this.isRegister
      ? this.authApi.register(request).pipe(switchMap(() => this.authApi.login(request)))
      : this.authApi.login(request);

    operation.subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/characters';
        void this.router.navigateByUrl(returnUrl.startsWith('/') ? returnUrl : '/characters');
      },
      error: (error) => {
        this.errorMessage.set(apiErrorMessage(error, 'No fue posible completar la autenticación.'));
        this.pending.set(false);
      },
    });
  }

  protected fieldError(field: 'username' | 'password'): string {
    const errors = this.credentialsForm[field]().errors();
    return errors[0]?.message?.toString() ?? '';
  }
}

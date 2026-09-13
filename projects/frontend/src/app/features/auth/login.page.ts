import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  FieldTree,
  form,
  FormField,
  maxLength,
  minLength,
  pattern,
  required,
  submit,
  validate,
} from '@angular/forms/signals';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom, map } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ApiError, isApiError } from '../../core/errors/api-error.model';
import { translateFieldError } from '../../core/errors/error-messages';
import { ThemeService } from '../../core/theme/theme.service';
import { ErrorAlertComponent } from '../../shared/ui/error-alert.component';
import { ThemeToggleComponent } from '../../core/layout/theme-toggle.component';

export type AuthMode = 'login' | 'register';

interface Credentials {
  username: string;
  password: string;
}

interface RegisterModel extends Credentials {
  confirmPassword: string;
}

const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;

/**
 * The only screen reachable without a session. Login and account creation live here as two
 * tabs so that `/login` stays the single public entry point.
 */
@Component({
  selector: 'app-login-page',
  imports: [FormField, ErrorAlertComponent, ThemeToggleComponent],
  templateUrl: './login.page.html',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly theme = inject(ThemeService);
  protected readonly mode = signal<AuthMode>(this.initialMode());
  protected readonly showPassword = signal(false);
  protected readonly error = signal<ApiError | null>(null);
  protected readonly notice = signal<string | null>(null);

  private readonly queryParams = toSignal(this.route.queryParamMap, { requireSync: true });
  protected readonly sessionExpired = computed(() => this.queryParams().get('reason') === 'expired');
  private readonly returnUrl = computed(() => {
    const url = this.queryParams().get('returnUrl') ?? '';
    // Only same-app absolute paths; never an external redirect and never back to /login.
    return url.startsWith('/') && !url.startsWith('//') && !url.startsWith('/login') ? url : '/characters';
  });

  protected readonly loginModel = signal<Credentials>({ username: '', password: '' });
  protected readonly loginForm = form(this.loginModel, (schema) => {
    required(schema.username, { message: 'Introduce tu nombre de usuario.' });
    required(schema.password, { message: 'Introduce tu contraseña.' });
  });

  protected readonly registerModel = signal<RegisterModel>({
    username: '',
    password: '',
    confirmPassword: '',
  });
  protected readonly registerForm = form(this.registerModel, (schema) => {
    required(schema.username, { message: 'Elige un nombre de usuario.' });
    minLength(schema.username, 3, { message: 'Mínimo 3 caracteres.' });
    maxLength(schema.username, 64, { message: 'Máximo 64 caracteres.' });
    pattern(schema.username, USERNAME_PATTERN, {
      message: 'Solo letras, números, ".", "_" y "-" (sin espacios).',
    });
    required(schema.password, { message: 'Elige una contraseña.' });
    minLength(schema.password, 8, { message: 'Mínimo 8 caracteres.' });
    maxLength(schema.password, 72, { message: 'Máximo 72 caracteres.' });
    required(schema.confirmPassword, { message: 'Repite la contraseña.' });
    validate(schema.confirmPassword, ({ value, valueOf }) =>
      value() === valueOf(schema.password)
        ? undefined
        : { kind: 'mismatch', message: 'Las contraseñas no coinciden.' },
    );
  });

  protected readonly submitting = computed(
    () => this.loginForm().submitting() || this.registerForm().submitting(),
  );

  constructor() {
    this.route.queryParamMap
      .pipe(
        map((params) => params.get('mode')),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((mode) => {
        if (mode === 'register' || mode === 'login') {
          this.mode.set(mode);
        }
      });
  }

  protected switchMode(mode: AuthMode): void {
    this.mode.set(mode);
    this.error.set(null);
    this.notice.set(null);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { mode: mode === 'register' ? 'register' : null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  protected isInvalid(field: FieldTree<string>): boolean {
    return field().touched() && field().invalid();
  }

  protected firstError(field: FieldTree<string>): string {
    return field().errors()[0]?.message ?? 'Valor no válido.';
  }

  protected submitLogin(event: Event): void {
    event.preventDefault();
    this.error.set(null);
    void submit(this.loginForm, async () => {
      try {
        await firstValueFrom(this.auth.login(this.loginModel()));
        await this.router.navigateByUrl(this.returnUrl());
      } catch (error: unknown) {
        this.error.set(asApiError(error));
      }
      return undefined;
    });
  }

  protected submitRegister(event: Event): void {
    event.preventDefault();
    this.error.set(null);
    void submit(this.registerForm, async () => {
      const { username, password } = this.registerModel();
      try {
        await firstValueFrom(this.auth.register({ username, password }));
      } catch (error: unknown) {
        const apiError = asApiError(error);
        this.error.set(apiError);
        return this.serverFieldErrors(apiError);
      }
      try {
        await firstValueFrom(this.auth.login({ username, password }));
        await this.router.navigateByUrl(this.returnUrl());
      } catch {
        // Account exists but the automatic login failed: let the user sign in manually.
        this.loginModel.set({ username, password: '' });
        this.notice.set('Cuenta creada. Inicia sesión con tu nueva contraseña.');
        this.switchMode('login');
      }
      return undefined;
    });
  }

  /** Maps backend validation details to the register fields so they show next to the input. */
  private serverFieldErrors(error: ApiError) {
    const fields: Record<string, FieldTree<string> | undefined> = {
      username: this.registerForm.username,
      password: this.registerForm.password,
    };
    return error.details
      .filter((detail) => fields[detail.field] !== undefined)
      .map((detail) => ({
        kind: 'server',
        message: translateFieldError(detail),
        field: fields[detail.field] as FieldTree<string>,
      }));
  }

  private initialMode(): AuthMode {
    return this.route.snapshot.queryParamMap.get('mode') === 'register' ? 'register' : 'login';
  }
}

function asApiError(error: unknown): ApiError {
  return isApiError(error)
    ? error
    : new ApiError({ status: 0, code: 'UNKNOWN_ERROR', userMessage: 'Ha ocurrido un error inesperado.' });
}

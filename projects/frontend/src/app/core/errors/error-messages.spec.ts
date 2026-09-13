import { anApiError } from '../../../testing/test-data';
import { describeError, fieldErrors, translateFieldError } from './error-messages';

describe('error-messages', () => {
  it('uses the context to pick a more specific message', () => {
    const notFound = anApiError({ status: 404, code: 'NOT_FOUND', userMessage: 'genérico' });
    expect(describeError(notFound)).toBe('genérico');
    expect(describeError(notFound, 'character')).toContain('Ese personaje no existe');
    expect(describeError(notFound, 'favorites')).toContain('no está en tus favoritos');
  });

  it('login 401 is a credentials problem, not an expired session', () => {
    const unauthorized = anApiError({ status: 401, code: 'UNAUTHORIZED', userMessage: 'sesión' });
    expect(describeError(unauthorized, 'login')).toBe('Usuario o contraseña incorrectos.');
  });

  it('translates javax validation messages keeping the field label', () => {
    expect(translateFieldError({ field: 'password', message: 'size must be between 8 and 72' })).toBe(
      'La contraseña debe tener entre 8 y 72 caracteres.',
    );
    expect(translateFieldError({ field: 'username', message: 'must not be blank' })).toBe(
      'El nombre de usuario es obligatorio.',
    );
    expect(
      translateFieldError({ field: 'username', message: "only letters, digits, '.', '_' and '-' are allowed" }),
    ).toBe('El nombre de usuario solo admite letras, números, ".", "_" y "-".');
    expect(translateFieldError({ field: 'size', message: 'must be less than or equal to 100' })).toBe(
      'El tamaño de página debe ser como máximo 100.',
    );
  });

  it('falls back to the raw message for unknown fields and rules', () => {
    expect(translateFieldError({ field: 'foo', message: 'weird rule' })).toBe('El campo "foo": weird rule');
    expect(fieldErrors(anApiError({ details: [{ field: 'page', message: 'must be greater than or equal to 0' }] }))).toEqual([
      'La página debe ser como mínimo 0.',
    ]);
  });
});

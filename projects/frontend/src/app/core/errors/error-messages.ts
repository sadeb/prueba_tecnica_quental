import { ApiError, ApiFieldError } from './api-error.model';

/** Where the error is being shown; lets the copy speak about characters, favorites or login. */
export type ErrorContext =
  | 'generic'
  | 'login'
  | 'register'
  | 'characters'
  | 'character'
  | 'related'
  | 'favorites';

const BY_CODE: Readonly<Record<string, string>> = {
  NETWORK_ERROR:
    'No hay conexión con el servidor. Comprueba tu red o que el portal (backend) esté encendido.',
  TIMEOUT: 'El servidor tarda demasiado en responder. Puede que ande cruzando dimensiones; inténtalo de nuevo.',
  VALIDATION_ERROR: 'Algunos datos no son válidos. Revisa los campos marcados.',
  UNAUTHORIZED: 'Tu sesión ha caducado o no es válida. Inicia sesión de nuevo.',
  FORBIDDEN: 'No tienes permiso para realizar esta acción.',
  NOT_FOUND: 'No hemos encontrado lo que buscas en esta dimensión.',
  METHOD_NOT_ALLOWED: 'Esa operación no está permitida.',
  CONFLICT: 'La operación entra en conflicto con el estado actual. Recarga e inténtalo de nuevo.',
  EXTERNAL_SERVICE_ERROR: 'La API pública de Rick and Morty no responde. Inténtalo más tarde.',
  SERVICE_UNAVAILABLE:
    'El servicio no está disponible ahora mismo (base de datos o grafo fuera de línea). Inténtalo en unos segundos.',
  INTERNAL_ERROR: 'Ha ocurrido un error inesperado en el servidor. Ni Rick sabría explicarlo.',
  UNKNOWN_ERROR: 'Ha ocurrido un error inesperado.',
};

const BY_STATUS: Readonly<Record<number, string>> = {
  0: BY_CODE['NETWORK_ERROR'],
  400: BY_CODE['VALIDATION_ERROR'],
  401: BY_CODE['UNAUTHORIZED'],
  403: BY_CODE['FORBIDDEN'],
  404: BY_CODE['NOT_FOUND'],
  405: BY_CODE['METHOD_NOT_ALLOWED'],
  408: BY_CODE['TIMEOUT'],
  409: BY_CODE['CONFLICT'],
  500: BY_CODE['INTERNAL_ERROR'],
  502: BY_CODE['EXTERNAL_SERVICE_ERROR'],
  503: BY_CODE['SERVICE_UNAVAILABLE'],
  504: BY_CODE['TIMEOUT'],
};

const BY_CONTEXT: Readonly<Partial<Record<ErrorContext, Readonly<Record<number, string>>>>> = {
  login: {
    400: 'Revisa el nombre de usuario y la contraseña.',
    401: 'Usuario o contraseña incorrectos.',
  },
  register: {
    400: 'Revisa los datos del registro: hay campos que no cumplen las reglas.',
    409: 'Ese nombre de usuario ya está en uso. Elige otro.',
  },
  characters: {
    400: 'Los filtros no son válidos. Restablécelos e inténtalo de nuevo.',
  },
  character: {
    404: 'Ese personaje no existe o todavía no se ha sincronizado desde la API de Rick and Morty.',
  },
  related: {
    404: 'Ese personaje no existe o todavía no se ha sincronizado desde la API de Rick and Morty.',
    503: 'El grafo de relaciones (Neo4j) no está disponible: ahora mismo no podemos calcular los personajes relacionados.',
  },
  favorites: {
    404: 'Ese personaje no está en tus favoritos o ya no existe.',
    409: 'Ese personaje ya estaba en tus favoritos.',
  },
};

/** Generic Spanish message for an HTTP status / backend error code (used by the interceptor). */
export function messageFor(status: number, code: string): string {
  return BY_STATUS[status] ?? BY_CODE[code] ?? BY_CODE['UNKNOWN_ERROR'];
}

/** Context-aware message for pages: overrides the generic one when the context knows better. */
export function describeError(error: ApiError, context: ErrorContext = 'generic'): string {
  return BY_CONTEXT[context]?.[error.status] ?? error.userMessage;
}

const FIELD_LABELS: Readonly<Record<string, string>> = {
  username: 'El nombre de usuario',
  password: 'La contraseña',
  name: 'El nombre',
  status: 'El estado',
  species: 'La especie',
  gender: 'El género',
  page: 'La página',
  size: 'El tamaño de página',
  limit: 'El límite',
};

const FIELD_RULES: readonly (readonly [RegExp, (match: RegExpMatchArray) => string])[] = [
  [/must not be (blank|null|empty)/i, () => 'es obligatorio'],
  [/size must be between (\d+) and (\d+)/i, (m) => `debe tener entre ${m[1]} y ${m[2]} caracteres`],
  [/length must be between (\d+) and (\d+)/i, (m) => `debe tener entre ${m[1]} y ${m[2]} caracteres`],
  [/only letters, digits/i, () => 'solo admite letras, números, ".", "_" y "-"'],
  [/must be less than or equal to (\d+)/i, (m) => `debe ser como máximo ${m[1]}`],
  [/must be greater than or equal to (\d+)/i, (m) => `debe ser como mínimo ${m[1]}`],
  [/must match/i, () => 'tiene un formato no válido'],
  [/Failed to convert|invalid value|not one of/i, () => 'tiene un valor no permitido'],
];

/** Translates a backend field message (javax validation, English) keeping the field name. */
export function translateFieldError(detail: ApiFieldError): string {
  const label = FIELD_LABELS[detail.field] ?? `El campo "${detail.field}"`;
  for (const [pattern, render] of FIELD_RULES) {
    const match = detail.message.match(pattern);
    if (match !== null) {
      return `${label} ${render(match)}.`;
    }
  }
  return `${label}: ${detail.message}`;
}

export function fieldErrors(error: ApiError): readonly string[] {
  return error.details.map(translateFieldError);
}

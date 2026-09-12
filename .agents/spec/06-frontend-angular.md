# Requisito 5 · Interfaz de usuario en Angular

1. SPA en **Angular** con **Bootstrap** como base de estilos, que consume **exclusivamente la API propia**. El navegador nunca llama a la API externa.
2. Acceso a la API **encapsulado en servicios**, con un **interceptor** que resuelva centralmente el envío del token y el tratamiento uniforme de errores.
3. **Pantallas mínimas**: registro, inicio de sesión, listado de personajes con filtros y paginación, detalle de personaje (episodios, localizaciones y personajes relacionados), gestión de favoritos.
4. **Sesión**: rutas protegidas con guardas, persistencia del token entre recargas, respuesta controlada ante sesión caducada o no autorizada.
5. **Estados de carga, vacío y error** explícitos en cada vista; nunca un estado indeterminado ante fallo del backend.
6. **Separación** clara entre componentes de presentación y lógica de negocio / acceso a datos.

No se evalúa el acabado estético: se valora organización del código, uso idiomático del framework y tratamiento de los casos que no son happy path.

Relacionado:
- [conventions/angular.md](../conventions/angular.md), [references/angular-bootstrap.md](../references/angular-bootstrap.md)
- Workflows: [12-frontend-esqueleto.md](../workflows/12-frontend-esqueleto.md), [13-frontend-auth.md](../workflows/13-frontend-auth.md), [14-frontend-personajes.md](../workflows/14-frontend-personajes.md), [15-frontend-favoritos.md](../workflows/15-frontend-favoritos.md)

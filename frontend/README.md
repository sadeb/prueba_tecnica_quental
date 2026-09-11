# Frontend Angular

SPA standalone en Angular 22. El navegador consume exclusivamente la API propia bajo `/api/v1`.

```bash
npm ci
npm start
npm test -- --watch=false
npm run build
```

La aplicación organiza tipos de contrato, clientes HTTP, sesión, interceptor y guards en `core/`; las pantallas se cargan de forma diferida desde `features/`. No contiene acceso directo a Rick and Morty API.

El runtime fijado para desarrollo y contenedor es Node 24.19.0. Bootstrap aporta la base accesible de componentes y `styles.css` define la identidad visual del archivo interdimensional.

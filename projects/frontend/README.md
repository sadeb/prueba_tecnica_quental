# frontend · Portal Rick y Morty

SPA Angular 22.1 (standalone, zoneless, Signal Forms) con Bootstrap 5.3.8. Consume solo la API propia (`/api`).
Documentación completa, ejecución y decisiones: [README raíz](../../README.md) y [ADR-010](../../.agents/decisions/ADR-010-frontend-sesion-tema-errores.md).

```bash
npm ci            # dependencias (Node 24)
npm start         # ng serve con proxy /api -> http://localhost:4080
npm test          # ng test --watch=false (Vitest + jsdom)
npm run build     # producción en dist/frontend/browser (lo usa el Dockerfile)
```

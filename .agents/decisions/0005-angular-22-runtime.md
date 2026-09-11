# ADR 0005: Angular 22 y runtime frontend

- Estado: Aceptado, en implementación
- Fecha: 2026-09-11
- Alcance de implementación: Fases 2 y 7

## Contexto

El scaffold existente declara Angular 20.3.31, aunque el usuario solicitó explícitamente Angular 22. La matriz oficial de compatibilidad de Angular 22 requiere Node `^22.22.3`, `^24.15.0` o `^26.0.0` y TypeScript 6.0.x. El Node local 22.12.0 no es compatible, pero el entorno dispone de Node 24.19.0.

## Decisión

- Actualizar Angular y Angular CLI a la línea 22.1.x.
- Fijar Node 24.19.0 para desarrollo, pruebas y construcción mediante `.nvmrc` y `engines`.
- Usar TypeScript 6.0.x y RxJS 7.8.x.
- Adoptar componentes standalone, signals, carga diferida de rutas, interceptores y guards funcionales.
- Usar Signal Forms para formularios nuevos de autenticación.
- Mantener frontend y backend como proyectos independientes y usar proxy solo durante desarrollo.

## Consecuencias

- Los desarrolladores con Node 22.12 deberán actualizarlo antes de usar Angular CLI.
- El lockfile se regenerará con el runtime compatible.
- No se mantendrá compatibilidad de build con Angular 20.

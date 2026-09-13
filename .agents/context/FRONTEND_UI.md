# Contexto de interfaz responsive

Estado: `IMPLEMENTADO; MANTENER EN CAMBIOS DE UI`

Este documento concentra las decisiones de interfaz que deben preservarse al crear o
modificar pantallas Angular. Complementa `ARCHITECTURE.md`, `STACK.md` y las reglas
canónicas de `AGENTS.md`; no sustituye el contrato HTTP ni los guards.

## Alcance y fuentes de estilos

- `frontend/src/styles.css` contiene los tokens visuales, las reglas globales de
  accesibilidad, los controles compartidos y el contenedor `.page-shell`.
- `frontend/src/app/app.css` es responsable del shell: skip link, header, navegación,
  contenido principal y footer.
- Cada feature conserva sus reglas de composición en su propio fichero CSS. No añadir
  llamadas HTTP a componentes de presentación ni desplazar reglas globales a una
  feature sin una razón concreta.

## Reglas obligatorias

- Diseñar mobile-first. La composición inicial es una sola columna y se amplía mediante
  `min-width`; no invertir ese flujo con una base de escritorio y media queries
  `max-width` salvo que exista una necesidad local documentada.
- Usar los breakpoints compartidos: `36rem` (576 px), `48rem` (768 px) y `62rem`
  (992 px). El único breakpoint de altura actual es el de autenticación
  `min-width: 62rem` + `max-height: 48rem`, necesario para que el login entre
  en laptops de poca altura.
- Usar propiedades lógicas y dimensiones fluidas: `inline-size`, `block-size`,
  `minmax(0, 1fr)`, `clamp()` y `vmin` cuando la altura y la anchura influyan a la vez.
  Evitar anchos, alturas y columnas fijas que obliguen a scroll horizontal.
- Toda celda flex o grid que pueda contener texto largo debe poder reducirse con
  `min-inline-size: 0`. Para texto inevitablemente largo, preferir wrapping o
  `text-overflow: ellipsis` cuando truncar sea semánticamente aceptable.
- Las imágenes y SVG deben respetar `max-inline-size: 100%`. No incorporar imágenes de
  tamaño fijo sin preservar su adaptación dentro de la tarjeta o el contenedor.
- Botones, enlaces de acción, inputs y selects deben conservar un objetivo interactivo
  de al menos `2.75rem` (44 px). Mantener los estilos `:focus-visible` globales.
- Respetar `prefers-reduced-motion`; no introducir animaciones que dependan de que el
  usuario pueda percibir movimiento.

## Shell y rutas Angular

El host de `App` es una columna flexible de al menos `100dvh`; header y footer ocupan
su tamaño natural y `#main-content` consume el espacio restante. Esto evita el cálculo
frágil de alturas con `100vh - ...` y mantiene el footer al borde inferior cuando el
contenido cabe.

Angular inserta el host de una ruta junto a `router-outlet`, fuera de la encapsulación
del CSS de `App`. Por ello, `styles.css` contiene el selector global
`#main-content > router-outlet + *`, que convierte ese host en elemento flexible,
reducible y de ancho completo. No eliminarlo ni sustituirlo por un selector encapsulado
en `app.css`: las pantallas lazy volverían a dejar espacio vacío o a desbordarse.

En el header, la navegación ocupa una segunda fila y admite desplazamiento horizontal en
móvil; a partir de `62rem` vuelve a una fila de escritorio. No ocultar los enlaces de
navegación activos para resolver falta de espacio.

## Autenticación

- En móvil y tablet, `.auth-panel` se muestra antes de `.auth-intro`, para priorizar la
  tarea de acceso. La introducción permanece disponible después mediante desplazamiento
  vertical natural.
- A partir de `62rem`, el login usa dos columnas. En una laptop de poca altura
  se reduce padding, tamaño del título y márgenes para que el header, el formulario y el
  footer entren en el viewport.
- La marca decorativa del portal es puramente visual y debe permanecer contenida por
  `.auth-intro`; nunca debe producir scroll horizontal.
- No se muestra autorregistro ni enlace de alta pública. La pantalla informa que las
  cuentas nuevas dependen de un administrador.

## Administración de usuarios

- `/admin/users` conserva una sola columna en móvil y separa el protocolo de acceso del
  formulario a partir de `48rem`.
- El formulario crea únicamente cuentas con rol `USER`; no ofrece selección de rol ni
  inicia sesión automáticamente con las credenciales creadas.

## Sincronización

- La pantalla conserva el estado local con signals y deriva con `computed` la presencia
  de ejecuciones y el sondeo activo.
- Una ejecución manual recién iniciada se consulta cada 60 segundos hasta alcanzar un
  estado terminal; el sondeo se cancela al destruir el componente.
- Los códigos internos se presentan en español y el porcentaje se etiqueta como
  procesamiento de mensajes, para no confundir un 100 % procesado con una descarga
  completa.
- El historial identifica el disparador `Manual` o `Automática` y separa errores de
  descarga de los mensajes fallidos en Kafka.

## Patrones para nuevas pantallas

- Partir de `.page-shell` para el contenido de una página estándar.
- Empezar grids de tarjetas, filtros y paneles con una columna. Ampliarlos en los
  breakpoints compartidos usando `repeat(..., minmax(0, 1fr))`.
- En cabeceras y listas administrativas, permitir wrapping y apilar métricas, botones o
  metadatos antes de reducir la tipografía por debajo de un tamaño legible.
- Mantener paginación y acciones en una fila solo cuando haya espacio; en móvil puede
  distribuirse entre extremos o apilarse sin cortar etiquetas.
- No usar `min-height: calc(100vh - ...)` en una feature: usar el shell existente o una
  altura mínima propia únicamente cuando sea necesaria para el contenido.

## Validación requerida

Para cambios de layout, comprobar al menos `375x667`, `768x1024`, `1024x600` y
`1440x900`. En login, comprobar también `1366x768`.

- No debe haber scroll horizontal (`scrollWidth === clientWidth`).
- Los formularios, errores, títulos largos, navegación y footer deben ser alcanzables y
  legibles; el scroll vertical es correcto cuando el contenido real no cabe.
- Verificar visualmente tanto el estado principal como los estados loading, vacío o error
  modificados por la pantalla.
- Ejecutar `npm test -- --watch=false`, `npm run build` y `git diff --check` con un
  Node compatible con Angular 22. No añadir Playwright sin autorización explícita.

## Evidencia vigente

El 2026-09-11 se verificó la versión anterior de login, registro, catálogo, detalle y
página no encontrada en los viewports indicados, sin desbordamiento horizontal.
Favoritos y sincronización redirigieron a login en la comprobación sin sesión. Esa
evidencia es histórica porque después se retiró el registro público.

El 2026-09-13 se validó el login actualizado y, con una sesión administrativa ficticia,
`/admin/users` en `375x667`, `768x1024`, `1024x600` y `1440x900`. No hubo
desbordamiento horizontal, los controles midieron al menos 44 px en ambos ejes cuando
su contenido no exigía más espacio y el scroll vertical fue natural. El formulario se
mantiene en una columna hasta `62rem` para evitar cortes incómodos en tableta. La
evidencia de comandos se conserva en `HANDOFF.md`.

La pantalla de sincronización se validó tras incorporar origen, polling y mensajes
traducidos en `375x667`, `768x1024`, `1024x600` y `1440x900`, sin desbordamiento
horizontal.

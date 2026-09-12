# Objetivo y alcance

**Duración estimada**: 12–14 h. **Entrega**: repositorio Git. **Entorno**: Docker Compose.

## Objetivo
Evaluar criterio de diseño, calidad de código y dominio del stack en un escenario representativo:
- consumo de servicios externos,
- procesamiento a través de una cola de mensajes,
- persistencia relacional y en grafo,
- interfaz que consuma la API propia.

Está permitido usar IA; se valorará **comprender y justificar cada decisión técnica** en la entrevista posterior ([10-entrega.md](10-entrega.md)).

## Fuera de alcance (explícito)
No se pide: alta disponibilidad, particionado, seguridad avanzada, ajuste de rendimiento. Cada pieza de infraestructura en **su uso más simple y defendible**. Evitar sobreingeniería ([09-criterios-valoracion.md](09-criterios-valoracion.md)).

## Contexto funcional
Fuente: API pública Rick and Morty (`https://rickandmortyapi.com`). Flujo: consumir fuente → volcar a mensajería → persistir en BD propia → exponer API propia (datos sincronizados + funcionalidad de usuario) → SPA Angular sobre esa API.

Relacionado: [prioridades.md](prioridades.md), [01-entorno-y-versiones.md](01-entorno-y-versiones.md).

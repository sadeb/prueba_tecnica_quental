# Criterios de valoración

1. **Calidad del código**: legibilidad, limpieza, separación de responsabilidades, arquitectura.
2. **Modelo de datos**: corrección de relaciones y pertinencia del reparto relacional / grafo.
3. **Robustez de la sincronización**: idempotencia, tolerancia a fallos, uso proporcionado de la mensajería, **sin sobreingeniería**.
4. **Estructura del frontend**: composición de componentes, gestión de estado, aislamiento del acceso a la API.
5. **Coherencia del flujo de usuario** y estados de carga/error extremo a extremo.
6. **Calidad y pertinencia de las pruebas**.
7. **Economía de dependencias**: capacidades del framework antes que paquetes externos no esenciales.
8. **Coherencia del historial de trabajo** en el repositorio.

Implicaciones prácticas para los agentes:
- Cada dependencia nueva → justificar en ADR ([decisions/](../decisions/README.md)).
- Commits pequeños, coherentes y ordenados ([conventions/git-commits.md](../conventions/git-commits.md)). El agente **no** hace commits ([skill no-git-write](../skills/no-git-write/SKILL.md)).
- Preferir la solución simple que se pueda defender en entrevista.

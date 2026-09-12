# Requisito 1 · Capa de integración con el servicio externo

1. **Cliente desacoplado** para consumir la API externa, aislado de controladores y de la lógica de negocio.
2. **Tolerancia a fallos** del servicio remoto: tiempos de espera, errores de red y respuestas no satisfactorias.
3. **Transformación** de la respuesta externa a estructuras propias del dominio. No acoplar el modelo interno ni las entidades persistidas al formato del proveedor.
4. **Validación** de la respuesta externa antes de procesarla: no asumir que el formato es el esperado; reaccionar de forma controlada si no lo es.

Relacionado:
- Formato real de la fuente y sus inconsistencias: [references/rick-and-morty-api.md](../references/rick-and-morty-api.md)
- Cómo construirlo: [workflows/03-cliente-api-externa.md](../workflows/03-cliente-api-externa.md)
- Decisión sobre identificadores: [decisions/ADR-001-identificador-externo.md](../decisions/ADR-001-identificador-externo.md)

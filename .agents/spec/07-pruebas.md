# Requisito 6 · Pruebas automatizadas

1. Cobertura de los **casos relevantes**, con atención a los **casos límite** (no solo happy path).
2. Las pruebas que involucren el servicio externo deben **aislarse de la red real** (sin dependencia del servicio en vivo).
3. Al menos **una prueba del ciclo de sincronización más allá de la unidad**: publicación y consumo, o consumo y persistencia, **sin depender de infraestructura instalada manualmente** en la máquina del evaluador.
4. Al menos **una prueba en el frontend** sobre lógica no trivial: un servicio, una guarda o un componente con filtros.

Relacionado:
- [references/testing-backend.md](../references/testing-backend.md), [references/testing-frontend.md](../references/testing-frontend.md)
- Skill: [testing-aislado](../skills/testing-aislado/SKILL.md)
- Workflows: [16-pruebas-backend.md](../workflows/16-pruebas-backend.md), [17-pruebas-frontend.md](../workflows/17-pruebas-frontend.md)

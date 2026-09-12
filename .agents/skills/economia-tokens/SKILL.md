---
name: economia-tokens
description: Reglas para minimizar el consumo de tokens en este repositorio: cargar solo el contexto del paso actual, no leer ficheros grandes enteros, respuestas cortas, sin repetir contenido de ficheros existentes.
---

# economia-tokens

## Al empezar una tarea
1. Leer [AGENTS.md](../../../AGENTS.md) (ya en contexto si el agente lo carga automáticamente; no releer).
2. Abrir **solo** el workflow del paso y lo que este enlace directamente (1 spec, 1–2 references, 1–2 ADRs, 1 convención).
3. No abrir `README.md` de cada carpeta de `.agents/` salvo que no se sepa qué fichero buscar.

## Al leer código
- `grep`/`find` antes que `cat`; leer rangos de líneas, no ficheros enteros.
- No leer `node_modules/`, `target/`, `dist/`, `.angular/`, `package-lock.json`, `mvnw`, ficheros generados.
- No pedir la salida completa de un comando: pedir el fragmento ([no-run-commands](../no-run-commands/SKILL.md)).

## Al escribir
- Código completo del fichero solo si es nuevo o cambia > 50 %; si no, edición puntual.
- Sin comentarios que repitan el código. Sin javadoc en clases obvias.
- Respuestas al humano: resultado, ficheros tocados, "ejecuta y pega", commit propuesto. Sin resúmenes de lo que ya está en los ficheros.
- No copiar contenido de `spec/`, `references/` o ADRs en la respuesta: enlazar.

## Al documentar
- Actualizar el fichero de `.agents/` afectado en lugar de crear uno nuevo, salvo concepto realmente distinto.
- Un fichero > 80 líneas se divide.

## Señales de derroche (parar y corregir)
Releer un fichero ya leído en la misma sesión; cargar más de 6 ficheros de contexto para un paso; pegar logs completos; generar tests de getters/setters; explicar Spring o Angular al humano.

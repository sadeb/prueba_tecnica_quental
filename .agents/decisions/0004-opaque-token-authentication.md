# ADR 0004: Autenticación propia mediante tokens opacos

- Estado: Aceptado, no implementado
- Fecha: 2026-09-11
- Alcance de implementación: Fase 6

## Contexto

El usuario decidió implementar el bonus de autenticación propia sin emisor ni validador externo. JWT añade semántica y superficie de seguridad innecesarias para este alcance, especialmente cuando se requiere revocación inmediata.

## Decisión

- Generar cada token con un CSPRNG usando 32 bytes aleatorios y codificación Base64 URL-safe sin padding.
- Entregar el token plano únicamente como respuesta al login.
- Persistir un hash SHA-256 del token, nunca el valor plano, junto con usuario, instante de creación, expiración y revocación.
- Validar en cada petición el hash, expiración, revocación, estado del usuario y roles.
- Usar el esquema HTTP `Bearer` y revocar el token actual al cerrar sesión.
- Proteger sincronización y endpoints administrativos con el rol `ADMIN`.
- Crear o actualizar de forma idempotente el administrador inicial usando credenciales suministradas por variables de entorno, sin valores reales versionados.

La duración del token será configurable y se fijará con un valor por defecto documentado al implementar seguridad.

## Consecuencias

- La revocación es inmediata y sencilla.
- Cada petición autenticada necesita una consulta o estrategia de caché coherente; inicialmente se elegirá la consulta simple.
- SHA-256 es apropiado para localizar tokens de alta entropía; las contraseñas seguirán usando un algoritmo lento específico mediante Spring Security.
- No habrá refresh tokens ni integración OAuth en el alcance inicial.

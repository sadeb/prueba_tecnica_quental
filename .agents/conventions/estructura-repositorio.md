# Estructura del repositorio (monorepo)

```
.
├── AGENTS.md                 # entrada única para cualquier agente de IA
├── .agents/                  # contexto segmentado
├── README.md                 # instalación, ejecución, decisiones (entrega)
├── .gitignore
└── projects/                 # todo lo ejecutable vive aquí
    ├── docker-compose.yml    # infraestructura + backend + frontend (versiones fijadas)
    ├── .env                  # credenciales de desarrollo (versionado a propósito)
    ├── backend/              # Spring Boot 2.7, Maven wrapper
    │   ├── pom.xml
    │   ├── Dockerfile
    │   ├── .dockerignore
    │   └── src/main/java/com/quental/rickmorty/...   (ver java-spring.md)
    │   └── src/main/resources/{application.yml, application-test.yml, db/migration/}
    └── frontend/             # Angular CLI
        ├── package.json
        ├── Dockerfile
        ├── .dockerignore
        ├── nginx.conf
        └── src/app/...                                (ver angular.md)
```

- Un solo `README.md` raíz para la entrega ([spec/10-entrega.md](../spec/10-entrega.md)). `projects/backend/` y `projects/frontend/` pueden tener un README corto que enlace al raíz.
- `.gitignore` raíz cubre `target/`, `node_modules/`, `dist/`, `.angular/`, `*.log`, ficheros de IDE (y excluye de `*.jar` el `maven-wrapper.jar`).
- Nada de código en la raíz: solo documentación. Compose, `.env` y los dos proyectos cuelgan de `projects/`.
- Todos los comandos de Docker se ejecutan desde `projects/` (`cd projects && docker compose …`); Compose carga `.env` desde el directorio del fichero compose.

Relacionado: [nomenclatura.md](nomenclatura.md), [workflows/00-bootstrap-repositorio.md](../workflows/00-bootstrap-repositorio.md), [references/docker-compose.md](../references/docker-compose.md).

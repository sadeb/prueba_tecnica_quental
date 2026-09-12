# Estructura del repositorio (monorepo)

```
.
├── AGENTS.md                 # entrada única para cualquier agente de IA
├── .agents/                  # contexto segmentado
├── README.md                 # instalación, ejecución, decisiones (entrega)
├── docker-compose.yml
├── .env                      # credenciales de desarrollo (versionado a propósito)
├── backend/                  # Spring Boot 2.7, Maven wrapper
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/quental/rickmorty/...   (ver java-spring.md)
│   └── src/main/resources/{application.yml, application-test.yml, db/migration/}
└── frontend/                 # Angular CLI
    ├── package.json
    ├── Dockerfile
    ├── nginx.conf
    └── src/app/...                                (ver angular.md)
```

- Un solo `README.md` raíz para la entrega ([spec/10-entrega.md](../spec/10-entrega.md)). `backend/` y `frontend/` pueden tener un README corto que enlace al raíz.
- `.gitignore` raíz cubre `target/`, `node_modules/`, `dist/`, `.angular/`, `*.log`, ficheros de IDE.
- Nada de código en la raíz fuera de compose y documentación.

Relacionado: [nomenclatura.md](nomenclatura.md), [workflows/00-bootstrap-repositorio.md](../workflows/00-bootstrap-repositorio.md).

package com.quental.rickmorty.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata and the bearer security scheme. Whole class disappears when
 * SWAGGER_ENABLED=false (springdoc.api-docs.enabled), see ADR-009.
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI rickMortyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rick and Morty sync API")
                        .version("v1")
                        .description("Synchronised Rick and Morty data (PostgreSQL + Neo4j) with per-user favorites. "
                                + "Public read endpoints; favorites and admin require a bearer token from /api/auth/login."))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token issued by POST /api/auth/login")));
    }
}

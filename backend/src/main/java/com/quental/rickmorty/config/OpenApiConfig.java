package com.quental.rickmorty.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI applicationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rick and Morty Quental API")
                        .version("v1")
                        .description("API propia para catálogo sincronizado, usuarios y favoritos."))
                .components(new Components().addSecuritySchemes("opaqueBearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Opaque token")));
    }
}

package com.quental.rickmorty.config;

import com.quental.rickmorty.external.ExternalApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(ExternalApiProperties.class)
public class ExternalApiConfig {

    /** Dedicated RestTemplate with timeouts (spec/02 point 2). No rootUri: the client builds full URLs. */
    @Bean
    public RestTemplate rickAndMortyRestTemplate(RestTemplateBuilder builder, ExternalApiProperties properties) {
        return builder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
    }
}

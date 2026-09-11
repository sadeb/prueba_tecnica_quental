package com.quental.rickmorty.sync.external;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.quental.rickmorty.config.ExternalApiProperties;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class RickMortyClientTest {

    private WireMockServer server;
    private RickMortyClient client;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(0);
        server.start();
        ExternalApiProperties properties = new ExternalApiProperties();
        properties.setBaseUrl(server.baseUrl() + "/api");
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(Duration.ofSeconds(1));
        client = new RickMortyClient(new RestTemplateBuilder().build(), properties);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void validatesAndReturnsAProviderPage() {
        server.stubFor(get(urlPathEqualTo("/api/character"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"info\":{\"count\":1,\"pages\":1,\"next\":null,\"prev\":null},"
                                + "\"results\":[{\"id\":1,\"name\":\"Rick Sanchez\"}]}")));

        assertThat(client.fetchPage(ResourceType.CHARACTER, 1).getResults())
                .singleElement().extracting(node -> node.path("name").asText())
                .isEqualTo("Rick Sanchez");
    }

    @Test
    void rejectsAnUnexpectedProviderShape() {
        server.stubFor(get(urlPathEqualTo("/api/episode"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"results\":[]}")));

        assertThatThrownBy(() -> client.fetchPage(ResourceType.EPISODE, 1))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Unexpected page structure");
    }
}

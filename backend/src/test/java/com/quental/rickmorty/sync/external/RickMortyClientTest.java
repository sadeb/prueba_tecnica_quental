package com.quental.rickmorty.sync.external;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.quental.rickmorty.config.ExternalApiProperties;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class RickMortyClientTest {

    private WireMockServer server;
    private RickMortyClient client;
    private ExternalApiProperties properties;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(0);
        server.start();
        properties = new ExternalApiProperties();
        properties.setBaseUrl(server.baseUrl() + "/api");
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(Duration.ofSeconds(1));
        properties.setRequestInterval(Duration.ZERO);
        properties.setInitialBackoff(Duration.ZERO);
        properties.setMaxBackoff(Duration.ZERO);
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

    @Test
    void retriesRateLimitResponseAndReturnsTheRecoveredPage() {
        server.stubFor(get(urlPathEqualTo("/api/character"))
                .inScenario("rate limit")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0"))
                .willSetStateTo("available"));
        server.stubFor(get(urlPathEqualTo("/api/character"))
                .inScenario("rate limit")
                .whenScenarioStateIs("available")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"info\":{\"count\":1,\"pages\":1,\"next\":null,\"prev\":null},"
                                + "\"results\":[{\"id\":1,\"name\":\"Rick Sanchez\"}]}")));

        assertThat(client.fetchPage(ResourceType.CHARACTER, 1).getResults()).hasSize(1);
        server.verify(exactly(2), getRequestedFor(urlPathEqualTo("/api/character")));
    }

    @Test
    void failsAfterExhaustingRateLimitRetries() {
        properties.setMaxAttempts(3);
        server.stubFor(get(urlPathEqualTo("/api/location"))
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0")));

        assertThatThrownBy(() -> client.fetchPage(ResourceType.LOCATION, 2))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Unable to fetch location page 2")
                .hasRootCauseInstanceOf(org.springframework.web.client.HttpClientErrorException.TooManyRequests.class);
        server.verify(exactly(3), getRequestedFor(urlPathEqualTo("/api/location")));
    }
}

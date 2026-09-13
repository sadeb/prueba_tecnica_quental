package com.quental.rickmorty.external;

import com.quental.rickmorty.TestData;
import com.quental.rickmorty.common.ExternalServiceException;
import com.quental.rickmorty.common.InvalidExternalPayloadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

/** Isolated from the real network: MockRestServiceServer answers every request (spec/07 point 2). */
class RickAndMortyClientTest {

    private static final String BASE = "http://source.test/api";
    private static final String PAGE_1 = BASE + "/character?page=1";

    private MockRestServiceServer server;
    private RickAndMortyClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        ExternalApiProperties properties = new ExternalApiProperties();
        properties.setBaseUrl(BASE);
        properties.setMaxRetries(2);
        properties.setRetryBackoff(Duration.ZERO);
        client = new RickAndMortyClient(restTemplate, properties);
    }

    @Test
    void shouldParsePageWhenSourceAnswers200() {
        String body = TestData.aCharacterPageJson(42, BASE + "/character?page=2",
                TestData.anExternalCharacterJson(1, BASE + "/location/1", BASE + "/episode/1"));
        server.expect(requestTo(PAGE_1)).andExpect(method(GET)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        ExternalPage<ExternalCharacter> page = client.fetchCharacters(1);

        assertThat(page.getInfo().getPages()).isEqualTo(42);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().get(0).getName()).isEqualTo("Rick Sanchez");
        assertThat(page.getResults().get(0).getEpisode()).containsExactly(BASE + "/episode/1");
        server.verify();
    }

    @Test
    void shouldThrowPageNotFoundWhen404() {
        server.expect(requestTo(BASE + "/character?page=99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("{\"error\":\"There is nothing here\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchCharacters(99)).isInstanceOf(ExternalPageNotFoundException.class);
    }

    @Test
    void shouldRetryAndSucceedWhenFirstAttemptIs500() {
        server.expect(ExpectedCount.once(), requestTo(PAGE_1)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        server.expect(ExpectedCount.once(), requestTo(PAGE_1))
                .andRespond(withSuccess(TestData.aCharacterPageJson(1, null), MediaType.APPLICATION_JSON));

        ExternalPage<ExternalCharacter> page = client.fetchCharacters(1);

        assertThat(page.getResults()).isEmpty();
        assertThat(page.hasNext()).isFalse();
        server.verify();
    }

    @Test
    void shouldGiveUpWithExternalServiceExceptionWhenEveryAttemptIs5xx() {
        server.expect(ExpectedCount.times(3), requestTo(PAGE_1)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.fetchCharacters(1)).isInstanceOf(ExternalServiceException.class);
        server.verify();
    }

    @Test
    void shouldRetryWhenReadTimesOut() {
        server.expect(ExpectedCount.once(), requestTo(PAGE_1))
                .andRespond(request -> { throw new SocketTimeoutException("Read timed out"); });
        server.expect(ExpectedCount.once(), requestTo(PAGE_1))
                .andRespond(withSuccess(TestData.aCharacterPageJson(1, null), MediaType.APPLICATION_JSON));

        assertThat(client.fetchCharacters(1).getResults()).isEmpty();
        server.verify();
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenUnexpected4xx() {
        server.expect(requestTo(PAGE_1)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.fetchCharacters(1)).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldThrowInvalidPayloadWhenBodyIsNotJson() {
        server.expect(requestTo(PAGE_1)).andRespond(withSuccess("<html>maintenance</html>", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchCharacters(1)).isInstanceOf(InvalidExternalPayloadException.class);
    }

    @Test
    void shouldReturnPageWithNullResultsWhenFieldIsMissing() {
        server.expect(requestTo(PAGE_1)).andRespond(withSuccess("{\"info\":{\"count\":0}}", MediaType.APPLICATION_JSON));

        ExternalPage<ExternalCharacter> page = client.fetchCharacters(1);

        // The client does not judge the content: ExternalPayloadValidator rejects a page without results.
        assertThat(page.getResults()).isNull();
    }
}

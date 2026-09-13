package com.quental.rickmorty.external;

import com.quental.rickmorty.common.ExternalServiceException;
import com.quental.rickmorty.common.InvalidExternalPayloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Only gateway to the external source (spec/02 point 1); used exclusively by the sync producer.
 * Fault tolerance (point 2): timeouts on the RestTemplate, bounded retry with fixed backoff for
 * network errors and 5xx, typed exceptions for 404 (end of pagination), other statuses and bad bodies.
 * A retry loop is enough for the scope: resilience4j / spring-retry would be extra dependencies.
 */
@Component
public class RickAndMortyClient {

    private static final Logger log = LoggerFactory.getLogger(RickAndMortyClient.class);

    private static final ParameterizedTypeReference<ExternalPage<ExternalCharacter>> CHARACTER_PAGE =
            new ParameterizedTypeReference<ExternalPage<ExternalCharacter>>() { };
    private static final ParameterizedTypeReference<ExternalPage<ExternalEpisode>> EPISODE_PAGE =
            new ParameterizedTypeReference<ExternalPage<ExternalEpisode>>() { };
    private static final ParameterizedTypeReference<ExternalPage<ExternalLocation>> LOCATION_PAGE =
            new ParameterizedTypeReference<ExternalPage<ExternalLocation>>() { };

    private final RestTemplate restTemplate;
    private final ExternalApiProperties properties;

    public RickAndMortyClient(RestTemplate rickAndMortyRestTemplate, ExternalApiProperties properties) {
        this.restTemplate = rickAndMortyRestTemplate;
        this.properties = properties;
    }

    public ExternalPage<ExternalCharacter> fetchCharacters(int page) {
        return fetchPage("character", page, CHARACTER_PAGE);
    }

    public ExternalPage<ExternalEpisode> fetchEpisodes(int page) {
        return fetchPage("episode", page, EPISODE_PAGE);
    }

    public ExternalPage<ExternalLocation> fetchLocations(int page) {
        return fetchPage("location", page, LOCATION_PAGE);
    }

    private <T> ExternalPage<T> fetchPage(String resource, int page, ParameterizedTypeReference<ExternalPage<T>> type) {
        String url = properties.getBaseUrl() + "/" + resource + "?page=" + page;
        int attempts = properties.getMaxRetries() + 1;
        RuntimeException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ResponseEntity<ExternalPage<T>> response = restTemplate.exchange(url, HttpMethod.GET, null, type);
                ExternalPage<T> body = response.getBody();
                if (body == null) {
                    throw new InvalidExternalPayloadException("Empty body from " + url);
                }
                return body;
            } catch (HttpClientErrorException.NotFound ex) {
                throw new ExternalPageNotFoundException("Page " + page + " of " + resource + " not found");
            } catch (HttpServerErrorException | ResourceAccessException ex) {
                last = ex;
                log.warn("Attempt {}/{} to fetch {} failed: {}", attempt, attempts, url, ex.getMessage());
                if (attempt < attempts) {
                    sleep(properties.getRetryBackoff().toMillis());
                }
            } catch (HttpStatusCodeException ex) {
                throw new ExternalServiceException("Unexpected status " + ex.getRawStatusCode() + " from " + url, ex);
            } catch (RestClientException ex) {
                // Body could not be read/parsed as the expected structure
                throw new InvalidExternalPayloadException("Unreadable response from " + url + ": " + ex.getMessage(), ex);
            }
        }
        throw new ExternalServiceException("Giving up on " + url + " after " + attempts + " attempts", last);
    }

    private static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Interrupted while waiting to retry", ex);
        }
    }
}

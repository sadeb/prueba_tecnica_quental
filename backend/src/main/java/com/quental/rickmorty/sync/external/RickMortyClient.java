package com.quental.rickmorty.sync.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.quental.rickmorty.config.ExternalApiProperties;
import com.quental.rickmorty.sync.external.dto.RickMortyPageDto;
import com.quental.rickmorty.sync.external.dto.RickMortyPageInfoDto;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RickMortyClient {

    private static final Logger logger = LoggerFactory.getLogger(RickMortyClient.class);

    private final RestTemplate restTemplate;
    private final ExternalApiProperties properties;
    private final Object requestLock = new Object();
    private long nextRequestAtNanos;

    public RickMortyClient(RestTemplate restTemplate, ExternalApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public RickMortyPageDto<JsonNode> fetchPage(ResourceType resourceType, int page) {
        if (page < 1) {
            throw new IllegalArgumentException("External page is one-based");
        }
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .pathSegment(resourceType.getPath())
                .queryParam("page", page)
                .toUriString();
        int maxAttempts = Math.max(1, properties.getMaxAttempts());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<JsonNode> response = get(url);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    throw new ExternalApiException("External API returned an empty or unsuccessful response");
                }
                return validateAndMap(response.getBody(), resourceType);
            } catch (HttpStatusCodeException exception) {
                if (!isRetryable(exception.getStatusCode()) || attempt == maxAttempts) {
                    throw fetchException(resourceType, page, exception);
                }
                waitBeforeRetry(resourceType, page, attempt, retryAfter(exception));
            } catch (ResourceAccessException exception) {
                if (attempt == maxAttempts) {
                    throw fetchException(resourceType, page, exception);
                }
                waitBeforeRetry(resourceType, page, attempt, null);
            } catch (RestClientException exception) {
                throw fetchException(resourceType, page, exception);
            }
        }
        throw new IllegalStateException("Retry loop completed unexpectedly");
    }

    private ResponseEntity<JsonNode> get(String url) {
        synchronized (requestLock) {
            long waitNanos = nextRequestAtNanos - System.nanoTime();
            if (waitNanos > 0) {
                sleep(Duration.ofNanos(waitNanos));
            }
            nextRequestAtNanos = System.nanoTime() + nonNegative(properties.getRequestInterval()).toNanos();
            return restTemplate.getForEntity(url, JsonNode.class);
        }
    }

    private boolean isRetryable(HttpStatus status) {
        return status == HttpStatus.TOO_MANY_REQUESTS || status.is5xxServerError();
    }

    private void waitBeforeRetry(ResourceType type, int page, int attempt, Duration retryAfter) {
        Duration delay = retryAfter == null ? exponentialBackoff(attempt) : bounded(retryAfter);
        logger.warn("External API request for {} page {} failed transiently; retrying attempt {} of {} in {} ms",
                type.name().toLowerCase(), page, attempt + 1, Math.max(1, properties.getMaxAttempts()), delay.toMillis());
        sleep(delay);
    }

    private Duration exponentialBackoff(int failedAttempt) {
        long initialMillis = nonNegative(properties.getInitialBackoff()).toMillis();
        long maximumMillis = nonNegative(properties.getMaxBackoff()).toMillis();
        long multiplier = 1L << Math.min(failedAttempt - 1, 20);
        long baseMillis = Math.min(saturatedMultiply(initialMillis, multiplier), maximumMillis);
        long jitterBound = baseMillis / 4;
        long jitter = jitterBound == 0 ? 0 : ThreadLocalRandom.current().nextLong(jitterBound + 1);
        return Duration.ofMillis(Math.min(baseMillis + jitter, maximumMillis));
    }

    private Duration retryAfter(HttpStatusCodeException exception) {
        String value = exception.getResponseHeaders() == null
                ? null : exception.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return bounded(Duration.ofSeconds(Math.max(0, Long.parseLong(value.trim()))));
        } catch (NumberFormatException ignored) {
            try {
                Instant retryAt = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                return bounded(Duration.between(Instant.now(), retryAt));
            } catch (DateTimeParseException invalidHeader) {
                return null;
            }
        }
    }

    private Duration bounded(Duration duration) {
        Duration value = nonNegative(duration);
        Duration maximum = nonNegative(properties.getMaxBackoff());
        return value.compareTo(maximum) > 0 ? maximum : value;
    }

    private Duration nonNegative(Duration duration) {
        return duration == null || duration.isNegative() ? Duration.ZERO : duration;
    }

    private long saturatedMultiply(long value, long multiplier) {
        if (value == 0 || multiplier <= Long.MAX_VALUE / value) {
            return value * multiplier;
        }
        return Long.MAX_VALUE;
    }

    private void sleep(Duration duration) {
        try {
            long millis = nonNegative(duration).toMillis();
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Interrupted while waiting to call the external API", exception);
        }
    }

    private ExternalApiException fetchException(ResourceType resourceType, int page, RestClientException cause) {
        return new ExternalApiException(
                "Unable to fetch " + resourceType.name().toLowerCase() + " page " + page, cause);
    }

    private RickMortyPageDto<JsonNode> validateAndMap(JsonNode root, ResourceType type) {
        JsonNode infoNode = root.path("info");
        JsonNode resultsNode = root.path("results");
        if (!infoNode.isObject() || !resultsNode.isArray()
                || !infoNode.path("count").canConvertToInt()
                || !infoNode.path("pages").canConvertToInt()) {
            throw new ExternalApiException("Unexpected page structure for " + type.name().toLowerCase());
        }
        RickMortyPageInfoDto info = new RickMortyPageInfoDto();
        info.setCount(infoNode.path("count").asInt());
        info.setPages(infoNode.path("pages").asInt());
        info.setNext(nullableText(infoNode.get("next")));
        info.setPrev(nullableText(infoNode.get("prev")));
        if (info.getPages() < 1 || info.getCount() < 0) {
            throw new ExternalApiException("Invalid pagination metadata for " + type.name().toLowerCase());
        }

        List<JsonNode> results = new ArrayList<>();
        resultsNode.forEach(item -> {
            if (!item.isObject() || !item.path("id").canConvertToLong() || item.path("id").asLong() <= 0) {
                throw new ExternalApiException("Resource without a valid positive id");
            }
            results.add(item);
        });

        RickMortyPageDto<JsonNode> page = new RickMortyPageDto<>();
        page.setInfo(info);
        page.setResults(results);
        return page;
    }

    private String nullableText(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}

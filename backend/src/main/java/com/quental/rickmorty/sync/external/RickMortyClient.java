package com.quental.rickmorty.sync.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.quental.rickmorty.config.ExternalApiProperties;
import com.quental.rickmorty.sync.external.dto.RickMortyPageDto;
import com.quental.rickmorty.sync.external.dto.RickMortyPageInfoDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RickMortyClient {

    private final RestTemplate restTemplate;
    private final ExternalApiProperties properties;

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
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("External API returned an empty or unsuccessful response");
            }
            return validateAndMap(response.getBody(), resourceType);
        } catch (RestClientException exception) {
            throw new ExternalApiException("Unable to fetch " + resourceType.name().toLowerCase() + " page " + page, exception);
        }
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

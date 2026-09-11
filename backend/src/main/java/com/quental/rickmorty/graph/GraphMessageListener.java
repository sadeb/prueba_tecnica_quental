package com.quental.rickmorty.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.sync.messaging.ResourceMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GraphMessageListener {

    private final ObjectMapper objectMapper;
    private final GraphProjectionService graphProjectionService;

    public GraphMessageListener(ObjectMapper objectMapper, GraphProjectionService graphProjectionService) {
        this.objectMapper = objectMapper;
        this.graphProjectionService = graphProjectionService;
    }

    @KafkaListener(topics = "${app.topics.graph}")
    public void consume(String payload) throws JsonProcessingException {
        graphProjectionService.project(objectMapper.readValue(payload, ResourceMessage.class));
    }
}

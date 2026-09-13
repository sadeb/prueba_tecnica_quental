package com.quental.rickmorty.sync.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.sync.ResourceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ResourceMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ResourceMessageListener.class);

    private final ObjectMapper objectMapper;
    private final ResourceProcessor processor;

    public ResourceMessageListener(ObjectMapper objectMapper, ResourceProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @KafkaListener(topics = "${app.topics.raw}")
    public void consume(String payload) throws JsonProcessingException {
        processor.process(objectMapper.readValue(payload, ResourceMessage.class));
    }

    @KafkaListener(topics = "${app.topics.raw}.DLT")
    public void consumeDeadLetter(String payload) {
        try {
            ResourceMessage message = objectMapper.readValue(payload, ResourceMessage.class);
            processor.markFailed(message);
            logger.error("Resource message {} moved to DLT", message.getMessageId());
        } catch (JsonProcessingException exception) {
            logger.error("Unreadable message moved to DLT", exception);
        }
    }
}

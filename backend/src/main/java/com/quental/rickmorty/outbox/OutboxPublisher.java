package com.quental.rickmorty.outbox;

import com.quental.rickmorty.config.OutboxProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate,
                           OutboxProperties properties) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:2s}")
    @Transactional
    public void publishBatch() {
        List<OutboxEventEntity> events = repository.findPublishable(PageRequest.of(0, properties.getBatchSize()));
        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get(10, TimeUnit.SECONDS);
                event.published();
            } catch (Exception exception) {
                logger.warn("Unable to publish outbox event {}", event.getId(), exception);
                event.failed(exception.getMessage());
            }
            repository.save(event);
        }
    }
}

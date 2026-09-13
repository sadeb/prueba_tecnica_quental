package com.quental.rickmorty.config;

import com.quental.rickmorty.sync.SyncProperties;
import com.quental.rickmorty.sync.SyncRunService;
import com.quental.rickmorty.sync.consumer.CountingDltRecoverer;
import com.quental.rickmorty.sync.message.InvalidMessageException;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Consumer side (ADR-006): 3 attempts with 1 s backoff, then the record goes to <topic><dlt-suffix> and
 * the consumer moves on. Deserialisation/validation errors are not retried. AckMode.RECORD with
 * enable-auto-commit=false: the offset is committed only after the listener returns or after the
 * DLT publish succeeded, never before persistence.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final long RETRY_INTERVAL_MS = 1000L;
    private static final long MAX_RETRIES = 2L;

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
                                                 SyncProperties syncProperties,
                                                 SyncRunService syncRunService,
                                                 SyncMessageCodec codec) {
        // Same suffix as the topics declared in KafkaTopicsConfig; same partition (1 partition everywhere).
        DeadLetterPublishingRecoverer dltRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (failedRecord, exception) -> new TopicPartition(syncProperties.dltOf(failedRecord.topic()), failedRecord.partition()));
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new CountingDltRecoverer(dltRecoverer, syncRunService, codec),
                new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        handler.addNotRetryableExceptions(InvalidMessageException.class);
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // Explicit factory: spring.kafka.listener.* properties are NOT applied (Boot's configurer is typed
        // <Object, Object>); everything the container needs is set here on purpose.
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}

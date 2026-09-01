package com.queueshield.incidentservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Retry/DLT policy applied to every {@code @KafkaListener} in this service.
 *
 * <p>A transient failure (DB momentarily unreachable, a brief network blip) gets 3 retries with
 * exponential backoff (0.5s, 1s, 2s). If it still fails, the record is published to
 * {@code <topic>.DLT} instead of blocking the partition forever or silently dropping data - a
 * human (or a future reprocessing job) can inspect the dead-letter topic later. This is the same
 * policy in every service in this system; it's duplicated per-service rather than shared because
 * each service's failure characteristics could reasonably diverge later.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> resolveDeadLetterTopic(record));

        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxElapsedTime(4_000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    private org.apache.kafka.common.TopicPartition resolveDeadLetterTopic(ConsumerRecord<?, ?> record) {
        return new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition());
    }
}

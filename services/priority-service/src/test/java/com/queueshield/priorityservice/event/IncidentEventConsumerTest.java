package com.queueshield.priorityservice.event;

import com.queueshield.priorityservice.priority.IncidentPriorityRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the shared long-lived Kafka broker and Redis instance (see README). Each test
 * makes up its own incident id (based on the current time) rather than relying on any real
 * incident-service data, since priority-service only ever needs the id as an opaque join key.
 */
@SpringBootTest
class IncidentEventConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private IncidentPriorityRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void consumingIncidentCreatedPersistsScoresCachesAndPublishesPrioritized() {
        long incidentId = uniqueIncidentId();
        Instant now = Instant.now();

        IncidentEvent event = new IncidentEvent(
                "evt-" + UUID.randomUUID(), incidentId, "Flooding near the bridge",
                "CRITICAL", "REPORTED", 30, 12, now, now);

        try (Consumer<String, String> verifier = prioritizedTopicConsumer()) {
            verifier.poll(Duration.ofMillis(500));

            kafkaTemplate.send(Topics.INCIDENT_CREATED, String.valueOf(incidentId), event);

            Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                var priority = repository.findByIncidentId(incidentId);
                assertThat(priority).isPresent();
                assertThat(priority.get().getScore()).isGreaterThan(0);
                assertThat(priority.get().getSeverityComponent()).isEqualTo(95.0); // CRITICAL base score
            });

            String cached = redisTemplate.opsForValue().get("priority:incident:" + incidentId);
            assertThat(cached).isNotNull().contains("\"incidentId\":" + incidentId);

            Optional<ConsumerRecord<String, String>> published = findRecordForIncident(verifier, incidentId);
            assertThat(published).isPresent();
        }
    }

    @Test
    void redeliveredOlderEventDoesNotRegressAnAlreadyComputedScore() {
        long incidentId = uniqueIncidentId();
        Instant newer = Instant.now();
        Instant older = newer.minus(Duration.ofHours(1));

        IncidentEvent newerEvent = new IncidentEvent("evt-newer", incidentId, "Incident", "CRITICAL", "REPORTED", 50, 20, newer, newer);
        kafkaTemplate.send(Topics.INCIDENT_CREATED, String.valueOf(incidentId), newerEvent);

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(repository.findByIncidentId(incidentId)).isPresent());

        double scoreAfterFirstEvent = repository.findByIncidentId(incidentId).orElseThrow().getScore();

        IncidentEvent olderEvent = new IncidentEvent("evt-older", incidentId, "Incident", "LOW", "REPORTED", 1, 0, older, older);
        kafkaTemplate.send(Topics.INCIDENT_UPDATED, String.valueOf(incidentId), olderEvent);

        Awaitility.await().pollDelay(3, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(repository.findByIncidentId(incidentId).orElseThrow().getScore()).isEqualTo(scoreAfterFirstEvent));
    }

    private long uniqueIncidentId() {
        return System.currentTimeMillis();
    }

    private Consumer<String, String> prioritizedTopicConsumer() {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers,
                "test-verifier-" + UUID.randomUUID(), "true");
        props.put("auto.offset.reset", "earliest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                props, new org.apache.kafka.common.serialization.StringDeserializer(),
                new org.apache.kafka.common.serialization.StringDeserializer());
        consumer.subscribe(java.util.List.of(Topics.INCIDENT_PRIORITIZED));
        return consumer;
    }

    private Optional<ConsumerRecord<String, String>> findRecordForIncident(Consumer<String, String> consumer, long incidentId) {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            Optional<ConsumerRecord<String, String>> match = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> String.valueOf(incidentId).equals(r.key()))
                    .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
}

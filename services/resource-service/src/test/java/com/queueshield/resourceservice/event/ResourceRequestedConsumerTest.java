package com.queueshield.resourceservice.event;

import com.queueshield.resourceservice.reservation.ReservationStatus;
import com.queueshield.resourceservice.reservation.ResourceReservationRepository;
import com.queueshield.resourceservice.resource.Resource;
import com.queueshield.resourceservice.resource.ResourceRepository;
import com.queueshield.resourceservice.resource.ResourceStatus;
import com.queueshield.resourceservice.resource.ResourceType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Runs against the shared long-lived Kafka broker (see README). Each test creates its own
 * Resource row (H2, fresh per test JVM run) and uses a random assignmentId, so results are
 * self-contained regardless of what else has been published to the shared topics.
 */
@SpringBootTest
class ResourceRequestedConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceReservationRepository reservationRepository;

    @Test
    void successfulRequestReservesOneUnitAndPublishesAssigned() {
        Resource resource = resourceRepository.save(Resource.builder()
                .name("Ambulance").type(ResourceType.VEHICLE).quantityTotal(3).quantityAvailable(3)
                .location("Depot").status(ResourceStatus.AVAILABLE).build());

        long assignmentId = uniqueId();
        try (Consumer<String, String> verifier = topicConsumer(Topics.RESOURCE_ASSIGNED)) {
            verifier.poll(Duration.ofMillis(500));

            publish(new ResourceRequestedEvent(UUID.randomUUID().toString(), assignmentId, resource.getId(), 1L, Instant.now()));

            Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                Resource reloaded = resourceRepository.findById(resource.getId()).orElseThrow();
                assertThat(reloaded.getQuantityAvailable()).isEqualTo(2);
            });

            assertThat(reservationRepository.findByAssignmentId(assignmentId)).isPresent()
                    .get().extracting(r -> r.getStatus()).isEqualTo(ReservationStatus.RESERVED);

            assertThat(findRecordForKey(verifier, String.valueOf(assignmentId))).isPresent();
        }
    }

    @Test
    void requestWithNoStockIsRejectedAndPublishesRejection() {
        Resource resource = resourceRepository.save(Resource.builder()
                .name("Last Boat").type(ResourceType.RESCUE_BOAT).quantityTotal(1).quantityAvailable(0)
                .location("Dock").status(ResourceStatus.DEPLETED).build());

        long assignmentId = uniqueId();
        try (Consumer<String, String> verifier = topicConsumer(Topics.RESOURCE_REQUEST_REJECTED)) {
            verifier.poll(Duration.ofMillis(500));

            publish(new ResourceRequestedEvent(UUID.randomUUID().toString(), assignmentId, resource.getId(), 1L, Instant.now()));

            Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(reservationRepository.findByAssignmentId(assignmentId)).isPresent()
                            .get().extracting(r -> r.getStatus()).isEqualTo(ReservationStatus.REJECTED));

            assertThat(findRecordForKey(verifier, String.valueOf(assignmentId))).isPresent();
        }
    }

    @Test
    void redeliveredDuplicateRequestDoesNotDoubleReserve() {
        Resource resource = resourceRepository.save(Resource.builder()
                .name("Generator").type(ResourceType.POWER_GENERATOR).quantityTotal(5).quantityAvailable(5)
                .location("Depot").status(ResourceStatus.AVAILABLE).build());

        long assignmentId = uniqueId();
        ResourceRequestedEvent event = new ResourceRequestedEvent(UUID.randomUUID().toString(), assignmentId, resource.getId(), 1L, Instant.now());

        publish(event);
        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(resourceRepository.findById(resource.getId()).orElseThrow().getQuantityAvailable()).isEqualTo(4));

        // Simulate Kafka at-least-once redelivery of the exact same logical request.
        publish(event);

        Awaitility.await().pollDelay(3, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(resourceRepository.findById(resource.getId()).orElseThrow().getQuantityAvailable()).isEqualTo(4));
    }

    private long uniqueId() {
        return System.currentTimeMillis() + Thread.currentThread().threadId();
    }

    private void publish(ResourceRequestedEvent event) {
        kafkaTemplate.send(Topics.RESOURCE_REQUESTED, String.valueOf(event.assignmentId()), event);
    }

    private Consumer<String, String> topicConsumer(String topic) {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, "test-verifier-" + UUID.randomUUID(), "true");
        props.put("auto.offset.reset", "earliest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                props, new org.apache.kafka.common.serialization.StringDeserializer(),
                new org.apache.kafka.common.serialization.StringDeserializer());
        consumer.subscribe(java.util.List.of(topic));
        return consumer;
    }

    private Optional<ConsumerRecord<String, String>> findRecordForKey(Consumer<String, String> consumer, String key) {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            Optional<ConsumerRecord<String, String>> match = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> key.equals(r.key()))
                    .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
}

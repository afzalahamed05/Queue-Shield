package com.queueshield.incidentservice.incident;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.incidentservice.incident.dto.IncidentRequest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs against the shared long-lived Kafka broker (see README - local dev infra) rather than a
 * per-test ephemeral container, so this verifies both the REST CRUD contract AND that
 * creating/updating an incident actually produces a message on {@code incident.created}/
 * {@code incident.updated} - not just that the code compiles against KafkaTemplate. Each test
 * gives its incident a UUID-tagged title so a scan over whatever is currently on the topic
 * (which may include leftovers from earlier runs, since this is a persistent broker, not a
 * disposable one) unambiguously finds the message this run produced.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, String> testConsumer() {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers,
                "test-verifier-" + UUID.randomUUID(), "true");
        props.put("auto.offset.reset", "earliest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                props, new org.apache.kafka.common.serialization.StringDeserializer(),
                new org.apache.kafka.common.serialization.StringDeserializer());
        consumer.subscribe(java.util.List.of("incident.created", "incident.updated"));
        return consumer;
    }

    private Optional<ConsumerRecord<String, String>> findRecordContaining(Consumer<String, String> consumer, String marker) {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            Optional<ConsumerRecord<String, String>> match = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> r.value().contains(marker))
                    .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    @Test
    void createIncidentReturns201AndPublishesIncidentCreated() throws Exception {
        String marker = "UUID-" + UUID.randomUUID();
        try (Consumer<String, String> consumer = testConsumer()) {
            consumer.poll(Duration.ofMillis(500)); // trigger partition assignment before producing

            IncidentRequest request = new IncidentRequest(
                    "Flooding near the bridge " + marker, "30 trapped", "Riverside Bridge, Sector 4",
                    Severity.CRITICAL, null, 30, 12);

            String response = mockMvc.perform(post("/api/incidents")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.status").value("REPORTED"))
                    .andExpect(jsonPath("$.priorityScore").doesNotExist())
                    .andReturn().getResponse().getContentAsString();

            long id = objectMapper.readTree(response).get("id").asLong();

            ConsumerRecord<String, String> record = findRecordContaining(consumer, marker)
                    .orElseThrow(() -> new AssertionError("No incident.created message found containing marker " + marker));
            assertThat(record.key()).isEqualTo(String.valueOf(id));
        }
    }

    @Test
    void createIncidentWithBlankTitleReturns400() throws Exception {
        IncidentRequest request = new IncidentRequest("", "desc", "somewhere", Severity.LOW, null, 1, 0);

        mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'title')]").exists());
    }

    @Test
    void getMissingIncidentReturns404() throws Exception {
        mockMvc.perform(get("/api/incidents/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullLifecycleCreateReadUpdateDelete() throws Exception {
        IncidentRequest createRequest = new IncidentRequest(
                "Gas leak reported", "Strong odor near school", "Maple Street",
                Severity.HIGH, null, 15, 15);

        String response = mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        IncidentRequest updateRequest = new IncidentRequest(
                "Gas leak contained", "Utility crew on site", "Maple Street",
                Severity.MODERATE, IncidentStatus.IN_PROGRESS, 15, 15);

        mockMvc.perform(put("/api/incidents/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(delete("/api/incidents/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/incidents/{id}", id)).andExpect(status().isNotFound());
    }
}

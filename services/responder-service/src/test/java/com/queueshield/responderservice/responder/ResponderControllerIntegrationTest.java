package com.queueshield.responderservice.responder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.responderservice.responder.dto.DispatchRequest;
import com.queueshield.responderservice.responder.dto.ResponderRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResponderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dispatchingAvailableResponderSucceedsAndPublishesResponderAssigned() throws Exception {
        long assignmentId = uniqueId();
        String response = createResponder("Unit 7", ResponderStatus.AVAILABLE);
        long id = objectMapper.readTree(response).get("id").asLong();

        try (Consumer<String, String> verifier = topicConsumer()) {
            verifier.poll(Duration.ofMillis(500));

            DispatchRequest dispatchRequest = new DispatchRequest(assignmentId, 1L);
            mockMvc.perform(post("/api/responders/{id}/dispatch", id)
                            .contentType("application/json").content(objectMapper.writeValueAsString(dispatchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DISPATCHED"));

            assertThat(findRecordForKey(verifier, String.valueOf(assignmentId))).isPresent();
        }

        mockMvc.perform(post("/api/responders/{id}/release", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void dispatchingOffDutyResponderReturns409() throws Exception {
        String response = createResponder("Unit 9", ResponderStatus.OFF_DUTY);
        long id = objectMapper.readTree(response).get("id").asLong();

        DispatchRequest dispatchRequest = new DispatchRequest(uniqueId(), 1L);
        mockMvc.perform(post("/api/responders/{id}/dispatch", id)
                        .contentType("application/json").content(objectMapper.writeValueAsString(dispatchRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void dispatchingAlreadyDispatchedResponderReturns409() throws Exception {
        String response = createResponder("Unit 11", ResponderStatus.AVAILABLE);
        long id = objectMapper.readTree(response).get("id").asLong();

        DispatchRequest first = new DispatchRequest(uniqueId(), 1L);
        mockMvc.perform(post("/api/responders/{id}/dispatch", id)
                        .contentType("application/json").content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        DispatchRequest second = new DispatchRequest(uniqueId(), 2L);
        mockMvc.perform(post("/api/responders/{id}/dispatch", id)
                        .contentType("application/json").content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    private String createResponder(String name, ResponderStatus status) throws Exception {
        ResponderRequest request = new ResponderRequest(name, ResponderRole.FIRE, "555-0100", status, "Station 3");
        return mockMvc.perform(post("/api/responders").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private long uniqueId() {
        return System.currentTimeMillis() + Thread.currentThread().threadId();
    }

    private Consumer<String, String> topicConsumer() {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, "test-verifier-" + UUID.randomUUID(), "true");
        props.put("auto.offset.reset", "earliest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                props, new org.apache.kafka.common.serialization.StringDeserializer(),
                new org.apache.kafka.common.serialization.StringDeserializer());
        consumer.subscribe(java.util.List.of("responder.assigned"));
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

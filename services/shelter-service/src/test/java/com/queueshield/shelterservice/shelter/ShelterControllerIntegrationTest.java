package com.queueshield.shelterservice.shelter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.shelterservice.shelter.dto.ShelterRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShelterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createShelterReturns201AndPublishesCapacityChanged() throws Exception {
        try (Consumer<String, String> verifier = topicConsumer()) {
            verifier.poll(Duration.ofMillis(500));

            ShelterRequest request = new ShelterRequest("Central High School Shelter", "100 Main St", 200, 50, "555-0200");
            String response = mockMvc.perform(post("/api/shelters").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.capacityAvailable").value(150))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andReturn().getResponse().getContentAsString();
            long id = objectMapper.readTree(response).get("id").asLong();

            assertThat(findRecordForKey(verifier, String.valueOf(id))).isPresent();
        }
    }

    @Test
    void shelterBecomesFullWhenAtCapacity() throws Exception {
        ShelterRequest request = new ShelterRequest("Small Shelter", "1 Elm St", 10, 10, "555-0201");
        mockMvc.perform(post("/api/shelters").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FULL"))
                .andExpect(jsonPath("$.capacityAvailable").value(0));
    }

    @Test
    void createWithOccupiedExceedingTotalReturns400() throws Exception {
        ShelterRequest request = new ShelterRequest("Bad Shelter", "1 Elm St", 5, 10, "555-0202");
        mockMvc.perform(post("/api/shelters").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteShelterReturns204ThenNotFound() throws Exception {
        ShelterRequest request = new ShelterRequest("Temp Shelter", "2 Elm St", 20, 0, "555-0203");
        String response = mockMvc.perform(post("/api/shelters").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/shelters/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/shelters/{id}", id)).andExpect(status().isNotFound());
    }

    private Consumer<String, String> topicConsumer() {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, "test-verifier-" + UUID.randomUUID(), "true");
        props.put("auto.offset.reset", "earliest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
                props, new org.apache.kafka.common.serialization.StringDeserializer(),
                new org.apache.kafka.common.serialization.StringDeserializer());
        consumer.subscribe(java.util.List.of("shelter.capacity-changed"));
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

package com.queueshield.assignmentservice.assignment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.assignmentservice.assignment.dto.AssignmentRequest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * True cross-service integration tests: creates real rows in responder-service and
 * resource-service (over HTTP, both reachable on the shared Docker network - see README) and
 * drives the full dispatch flow through assignment-service, verifying the *other* service's state
 * changed as a result. This is the only place in the test suite that exercises the actual network
 * boundary end to end rather than a single service in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AssignmentDispatchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestClient responderServiceClient = RestClient.builder()
            .baseUrl(System.getenv().getOrDefault("RESPONDER_SERVICE_URL", "http://responder-service:8086"))
            .build();

    private final RestClient resourceServiceClient = RestClient.builder()
            .baseUrl(System.getenv().getOrDefault("RESOURCE_SERVICE_URL", "http://resource-service:8085"))
            .build();

    @Test
    void dispatchingRealResponderMarksItDispatchedInResponderService() throws Exception {
        long responderId = createResponder("Cross-Service Test Unit");

        AssignmentRequest request = new AssignmentRequest(1L, responderId, null, null, null);
        mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        String status = responderServiceClient.get().uri("/api/responders/{id}", responderId)
                .retrieve().body(JsonNode.class).get("status").asText();
        assertThat(status).isEqualTo("DISPATCHED");
    }

    @Test
    void dispatchingAlreadyDispatchedRealResponderReturns409AndAssignmentIsNotCreated() throws Exception {
        long responderId = createResponder("Already Busy Unit");
        // First dispatch succeeds directly against responder-service.
        responderServiceClient.post().uri("/api/responders/{id}/dispatch", responderId)
                .body(Map.of("assignmentId", 999_999, "incidentId", 1))
                .retrieve().toBodilessEntity();

        AssignmentRequest request = new AssignmentRequest(1L, responderId, null, null, null);
        mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void requestingRealResourceReservesOneUnitViaTheAsyncSaga() throws Exception {
        long resourceId = createResource("Cross-Service Ambulance", 3);

        AssignmentRequest request = new AssignmentRequest(1L, null, resourceId, null, null);
        String response = mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceRequestStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long assignmentId = objectMapper.readTree(response).get("id").asLong();

        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            mockMvc.perform(get("/api/assignments/{id}", assignmentId))
                    .andExpect(jsonPath("$.resourceRequestStatus").value("ASSIGNED"));
        });

        int quantityAvailable = resourceServiceClient.get().uri("/api/resources/{id}", resourceId)
                .retrieve().body(JsonNode.class).get("quantityAvailable").asInt();
        assertThat(quantityAvailable).isEqualTo(2);
    }

    private long createResponder(String name) {
        JsonNode body = responderServiceClient.post().uri("/api/responders")
                .body(Map.of("name", name, "role", "FIRE", "phone", "555-0100", "status", "AVAILABLE", "currentLocation", "Station 1"))
                .retrieve().body(JsonNode.class);
        return body.get("id").asLong();
    }

    private long createResource(String name, int quantity) {
        JsonNode body = resourceServiceClient.post().uri("/api/resources")
                .body(Map.of("name", name, "type", "VEHICLE", "quantityTotal", quantity, "quantityAvailable", quantity, "location", "Depot"))
                .retrieve().body(JsonNode.class);
        return body.get("id").asLong();
    }
}

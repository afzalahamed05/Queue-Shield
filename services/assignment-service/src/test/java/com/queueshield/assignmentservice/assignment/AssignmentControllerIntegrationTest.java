package com.queueshield.assignmentservice.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.assignmentservice.assignment.dto.AssignmentRequest;
import com.queueshield.assignmentservice.assignment.dto.AssignmentStatusUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the create flow against the REAL responder-service (over HTTP, on the shared Docker
 * network - see README local dev setup) rather than mocking it: dispatch is the one part of this
 * service where "does the HTTP call actually work end to end" is the whole point of the test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AssignmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWithNoTargetsReturns409() throws Exception {
        AssignmentRequest request = new AssignmentRequest(1L, null, null, null, null);
        mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithOnlyResourceIdIsPendingResourceRequest() throws Exception {
        AssignmentRequest request = new AssignmentRequest(1L, null, 999_999L, null, "test note");
        mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.resourceRequestStatus").value("PENDING"));
    }

    @Test
    void updateStatusToCompletedSucceeds() throws Exception {
        AssignmentRequest request = new AssignmentRequest(1L, null, null, 5L, null);
        String response = mockMvc.perform(post("/api/assignments").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        AssignmentStatusUpdateRequest completeRequest = new AssignmentStatusUpdateRequest(AssignmentStatus.COMPLETED);
        mockMvc.perform(patch("/api/assignments/{id}/status", id)
                        .contentType("application/json").content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}

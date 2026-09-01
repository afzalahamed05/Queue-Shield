package com.queueshield.incident;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.incident.dto.IncidentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createIncidentReturns201WithComputedPriority() throws Exception {
        IncidentRequest request = new IncidentRequest(
                "Flooding near the bridge", "Water level rising fast", "Riverside Bridge, Sector 4",
                com.queueshield.priority.Severity.CRITICAL, null, 30, 12);

        mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("REPORTED"))
                .andExpect(jsonPath("$.priorityScore").isNumber())
                .andExpect(jsonPath("$.priorityTier").exists())
                .andExpect(jsonPath("$.priorityBreakdown.severityComponent").value(95.0));
    }

    @Test
    void createIncidentWithBlankTitleReturns400WithFieldErrors() throws Exception {
        IncidentRequest request = new IncidentRequest(
                "", "desc", "somewhere", com.queueshield.priority.Severity.LOW, null, 1, 0);

        mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'title')]").exists());
    }

    @Test
    void createIncidentWithVulnerableCountExceedingPeopleAffectedReturns400() throws Exception {
        IncidentRequest request = new IncidentRequest(
                "Test incident", "desc", "somewhere", com.queueshield.priority.Severity.LOW, null, 5, 10);

        mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMissingIncidentReturns404() throws Exception {
        mockMvc.perform(get("/api/incidents/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void fullLifecycleCreateReadUpdateDelete() throws Exception {
        IncidentRequest createRequest = new IncidentRequest(
                "Gas leak reported", "Strong odor near school", "Maple Street",
                com.queueshield.priority.Severity.HIGH, null, 15, 15);

        String response = mockMvc.perform(post("/api/incidents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/incidents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gas leak reported"));

        IncidentRequest updateRequest = new IncidentRequest(
                "Gas leak contained", "Utility crew on site", "Maple Street",
                com.queueshield.priority.Severity.MODERATE, IncidentStatus.IN_PROGRESS, 15, 15);

        mockMvc.perform(put("/api/incidents/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gas leak contained"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(delete("/api/incidents/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/incidents/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listIncidentsIsPaginatedAndSortedByPriorityDescendingByDefault() throws Exception {
        IncidentRequest lowSeverity = new IncidentRequest(
                "Minor pothole", "desc", "Elm Street", com.queueshield.priority.Severity.LOW, null, 1, 0);
        IncidentRequest criticalSeverity = new IncidentRequest(
                "Building collapse", "desc", "Downtown", com.queueshield.priority.Severity.CRITICAL, null, 50, 20);

        mockMvc.perform(post("/api/incidents").contentType("application/json")
                .content(objectMapper.writeValueAsString(lowSeverity))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/incidents").contentType("application/json")
                .content(objectMapper.writeValueAsString(criticalSeverity))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/incidents").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Building collapse"));
    }
}

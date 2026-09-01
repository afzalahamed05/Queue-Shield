package com.queueshield.resourceservice.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueshield.resourceservice.resource.dto.ResourceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createResourceReturns201() throws Exception {
        ResourceRequest request = new ResourceRequest("Ambulance Unit 4", ResourceType.VEHICLE, 5, 5, "Central Depot");

        mockMvc.perform(post("/api/resources").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.quantityAvailable").value(5));
    }

    @Test
    void createWithAvailableExceedingTotalReturns400() throws Exception {
        ResourceRequest request = new ResourceRequest("Bad Resource", ResourceType.OTHER, 1, 5, "Somewhere");

        mockMvc.perform(post("/api/resources").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availabilityRatioReflectsCreatedResources() throws Exception {
        ResourceRequest request = new ResourceRequest("Water Crates", ResourceType.WATER, 10, 8, "Warehouse 2");
        mockMvc.perform(post("/api/resources").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/resources/availability-ratio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratio").isNumber());
    }

    @Test
    void getMissingResourceReturns404() throws Exception {
        mockMvc.perform(get("/api/resources/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void deleteResourceReturns204ThenNotFound() throws Exception {
        ResourceRequest request = new ResourceRequest("Generator", ResourceType.POWER_GENERATOR, 2, 2, "Depot");
        String response = mockMvc.perform(post("/api/resources").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/resources/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resources/{id}", id)).andExpect(status().isNotFound());
    }
}

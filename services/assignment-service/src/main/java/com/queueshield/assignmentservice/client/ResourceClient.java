package com.queueshield.assignmentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Only used for {@code release} - the reservation itself goes through the async ResourceRequested/Assigned saga (see event package). */
@Component
@Slf4j
public class ResourceClient {

    private final RestClient restClient;

    public ResourceClient(@Value("${resource-service.base-url:http://resource-service:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void release(Long resourceId) {
        try {
            restClient.post().uri("/api/resources/{id}/release", resourceId).retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Failed to release resource {}: {}", resourceId, ex.getMessage());
        }
    }
}

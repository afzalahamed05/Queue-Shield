package com.queueshield.assignmentservice.client;

import com.queueshield.assignmentservice.common.exception.BusinessRuleViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Synchronous by design - see the README's "why sync vs async per operation" section. Dispatch
 * must not race (two coordinators can't both successfully dispatch the same responder), so the
 * caller needs an immediate, unambiguous answer rather than a fire-and-forget event.
 */
@Component
@Slf4j
public class ResponderClient {

    private final RestClient restClient;

    public ResponderClient(@Value("${responder-service.base-url:http://responder-service:8086}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void dispatch(Long responderId, Long assignmentId, Long incidentId) {
        try {
            restClient.post()
                    .uri("/api/responders/{id}/dispatch", responderId)
                    .body(new DispatchBody(assignmentId, incidentId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new BusinessRuleViolationException("Could not dispatch responder " + responderId + ": " + ex.getMessage());
        }
    }

    public void release(Long responderId) {
        try {
            restClient.post().uri("/api/responders/{id}/release", responderId).retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Failed to release responder {}: {}", responderId, ex.getMessage());
        }
    }

    private record DispatchBody(Long assignmentId, Long incidentId) {
    }
}

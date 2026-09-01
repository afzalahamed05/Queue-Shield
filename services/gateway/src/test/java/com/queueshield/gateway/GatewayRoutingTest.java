package com.queueshield.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs against the REAL running incident-service, resource-service, and shelter-service (on the
 * shared Docker network - see README) rather than mocks: the entire point of a gateway is that it
 * correctly forwards to another process over the network, which a mock can't verify.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "20000")
class GatewayRoutingTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebTestClient webTestClient;

    @Test
    void routesToResourceService() {
        webTestClient.get().uri("/api/resources")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void routesToShelterService() {
        webTestClient.get().uri("/api/shelters")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void routesUnknownPathTo404() {
        webTestClient.get().uri("/api/does-not-exist")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void repeatedIncidentCreationEventuallyGetsRateLimited() {
        Map<String, Object> body = Map.of(
                "title", "Rate limit test incident", "location", "Test Location",
                "severity", "LOW", "peopleAffected", 1, "vulnerablePopulationCount", 0);

        AtomicInteger sawTooManyRequests = new AtomicInteger(0);

        // Burst capacity is 10 with a slow refill (5/sec) - 20 rapid requests should trip the limiter.
        for (int i = 0; i < 20; i++) {
            HttpStatus status = (HttpStatus) webTestClient.post().uri("/api/incidents")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();
            if (status == HttpStatus.TOO_MANY_REQUESTS) {
                sawTooManyRequests.incrementAndGet();
            }
        }

        org.assertj.core.api.Assertions.assertThat(sawTooManyRequests.get()).isGreaterThan(0);
    }
}

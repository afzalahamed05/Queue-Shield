package com.queueshield.incidentservice.event;

import com.queueshield.incidentservice.incident.Incident;
import com.queueshield.incidentservice.incident.IncidentRepository;
import com.queueshield.incidentservice.incident.IncidentStatus;
import com.queueshield.incidentservice.incident.PriorityTier;
import com.queueshield.incidentservice.incident.Severity;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the shared long-lived Kafka broker (see README). Uses the fixed "incident-service"
 * consumer group, same as production - Kafka remembers this group's committed offset across test
 * JVM runs, so each run only ever sees messages it publishes itself, not history from earlier
 * runs. That's what makes it safe to assert on DB state without needing a disposable broker.
 */
@SpringBootTest
class IncidentPrioritizedConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private IncidentRepository incidentRepository;

    private Long incidentId;

    @BeforeEach
    void setUp() {
        Incident incident = incidentRepository.save(Incident.builder()
                .title("Test incident")
                .location("Somewhere")
                .severity(Severity.HIGH)
                .status(IncidentStatus.REPORTED)
                .peopleAffected(10)
                .vulnerablePopulationCount(2)
                .reportedAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        incidentId = incident.getId();
    }

    @Test
    void appliesPriorityFromEvent() {
        Instant computedAt = Instant.now();
        publish(new IncidentPrioritizedEvent("evt-1", incidentId, 72.5, "HIGH", computedAt, computedAt));

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Incident reloaded = incidentRepository.findById(incidentId).orElseThrow();
            assertThat(reloaded.getPriorityScore()).isEqualTo(72.5);
            assertThat(reloaded.getPriorityTier()).isEqualTo(PriorityTier.HIGH);
        });
    }

    @Test
    void ignoresOutOfOrderRedeliveryOfAnOlderEvent() {
        Instant newer = Instant.now();
        Instant older = newer.minus(1, ChronoUnit.HOURS);

        publish(new IncidentPrioritizedEvent("evt-newer", incidentId, 90.0, "CRITICAL", newer, newer));
        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(incidentRepository.findById(incidentId).orElseThrow().getPriorityScore()).isEqualTo(90.0));

        // A redelivered/older event must not regress the already-applied newer score.
        publish(new IncidentPrioritizedEvent("evt-older-redelivered", incidentId, 10.0, "LOW", older, older));

        Awaitility.await().pollDelay(3, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Incident reloaded = incidentRepository.findById(incidentId).orElseThrow();
            assertThat(reloaded.getPriorityScore()).isEqualTo(90.0);
            assertThat(reloaded.getPriorityTier()).isEqualTo(PriorityTier.CRITICAL);
        });
    }

    private void publish(IncidentPrioritizedEvent event) {
        kafkaTemplate.send(Topics.INCIDENT_PRIORITIZED, String.valueOf(event.incidentId()), event);
    }
}

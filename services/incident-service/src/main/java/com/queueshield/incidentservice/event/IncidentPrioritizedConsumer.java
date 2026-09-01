package com.queueshield.incidentservice.event;

import com.queueshield.incidentservice.incident.Incident;
import com.queueshield.incidentservice.incident.IncidentRepository;
import com.queueshield.incidentservice.incident.PriorityTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates the read-through priority cache on {@link Incident}. Idempotency here doesn't need a
 * dedup table: applying the same event twice sets the same value twice (harmless), and
 * {@code Incident#applyPriorityIfNewer} rejects a redelivered *older* event using
 * {@code computedAt}, so both duplicate delivery and out-of-order delivery are handled without
 * extra state. If the incident referenced no longer exists (e.g. deleted after the event was
 * published), we log and drop rather than retry forever - there's nothing to update.
 */
@Component
@Slf4j
public class IncidentPrioritizedConsumer {

    private final IncidentRepository incidentRepository;

    public IncidentPrioritizedConsumer(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @KafkaListener(topics = Topics.INCIDENT_PRIORITIZED, groupId = "incident-service")
    @Transactional
    public void onIncidentPrioritized(IncidentPrioritizedEvent event) {
        incidentRepository.findById(event.incidentId()).ifPresentOrElse(
                incident -> {
                    incident.applyPriorityIfNewer(event.score(), PriorityTier.valueOf(event.tier()), event.computedAt());
                    incidentRepository.save(incident);
                    log.info("Applied priority {} ({}) to incident {}", event.score(), event.tier(), event.incidentId());
                },
                () -> log.warn("Received IncidentPrioritized for unknown incident {} - dropping", event.incidentId())
        );
    }
}

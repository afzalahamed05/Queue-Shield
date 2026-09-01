package com.queueshield.incidentservice.event;

import com.queueshield.incidentservice.incident.Incident;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes the incident's current state. The Kafka message key is the incident id (as a
 * string) so that all events for one incident land on the same partition and are processed in
 * order by any single consumer instance - important because priority-service's out-of-order
 * guard (see {@code Incident#applyPriorityIfNewer} on the consuming side) only protects against
 * priority-service's own re-ordering, not against incident-service's create/update events
 * arriving out of order upstream.
 */
@Component
@Slf4j
public class IncidentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IncidentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreated(Incident incident) {
        publish(Topics.INCIDENT_CREATED, incident);
    }

    public void publishUpdated(Incident incident) {
        publish(Topics.INCIDENT_UPDATED, incident);
    }

    private void publish(String topic, Incident incident) {
        IncidentEvent event = new IncidentEvent(
                UUID.randomUUID().toString(),
                incident.getId(),
                incident.getTitle(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getPeopleAffected(),
                incident.getVulnerablePopulationCount(),
                incident.getReportedAt(),
                Instant.now()
        );
        String key = String.valueOf(incident.getId());
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for incident {}", topic, incident.getId(), ex);
            } else {
                log.debug("Published {} for incident {} to partition {}", topic, incident.getId(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}

package com.queueshield.incidentservice.event;

import java.time.Instant;

/**
 * Payload published to both {@code incident.created} and {@code incident.updated} - the shape is
 * identical ("here is the current state of this incident"), the topic itself carries the
 * semantic difference. Consumers (priority-service) don't need to distinguish create vs. update:
 * either way, they recompute from the state given, which is why using one payload type for both
 * is safe here (recomputation is idempotent by nature, not something that needs a diff).
 */
public record IncidentEvent(
        String eventId,
        Long incidentId,
        String title,
        String severity,
        String status,
        int peopleAffected,
        int vulnerablePopulationCount,
        Instant reportedAt,
        Instant occurredAt
) {
}

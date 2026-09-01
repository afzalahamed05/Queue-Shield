package com.queueshield.priorityservice.event;

import java.time.Instant;

/** Consumed from both {@code incident.created} and {@code incident.updated} - see incident-service's producer. */
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

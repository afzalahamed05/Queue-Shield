package com.queueshield.resourceservice.event;

import java.time.Instant;

/** Published by assignment-service when it wants to reserve one unit of a specific resource for a specific assignment. */
public record ResourceRequestedEvent(
        String eventId,
        Long assignmentId,
        Long resourceId,
        Long incidentId,
        Instant occurredAt
) {
}

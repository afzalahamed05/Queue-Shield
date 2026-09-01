package com.queueshield.resourceservice.event;

import java.time.Instant;

public record ResourceAssignedEvent(
        String eventId,
        Long assignmentId,
        Long resourceId,
        Long incidentId,
        Instant occurredAt
) {
}

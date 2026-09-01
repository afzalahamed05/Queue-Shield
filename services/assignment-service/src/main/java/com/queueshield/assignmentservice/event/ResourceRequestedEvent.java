package com.queueshield.assignmentservice.event;

import java.time.Instant;

public record ResourceRequestedEvent(
        String eventId,
        Long assignmentId,
        Long resourceId,
        Long incidentId,
        Instant occurredAt
) {
}

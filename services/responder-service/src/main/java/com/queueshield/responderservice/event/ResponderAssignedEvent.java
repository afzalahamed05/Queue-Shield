package com.queueshield.responderservice.event;

import java.time.Instant;

/** Published as a fan-out fact AFTER a synchronous dispatch already succeeded - see responder-service's README notes on why dispatch itself is sync, not this event. */
public record ResponderAssignedEvent(
        String eventId,
        Long assignmentId,
        Long responderId,
        Long incidentId,
        Instant occurredAt
) {
}

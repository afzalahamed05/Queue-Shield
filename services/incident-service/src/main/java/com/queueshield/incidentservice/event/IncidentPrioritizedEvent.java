package com.queueshield.incidentservice.event;

import java.time.Instant;

/**
 * Consumed from {@code incident.prioritized}, published by priority-service. Mirrors the shape
 * priority-service emits; kept minimal to what incident-service actually needs to display (the
 * full per-factor breakdown isn't stored here, only the headline score/tier - a consumer only
 * needs to keep what it will actually read back).
 */
public record IncidentPrioritizedEvent(
        String eventId,
        Long incidentId,
        double score,
        String tier,
        Instant computedAt,
        Instant occurredAt
) {
}

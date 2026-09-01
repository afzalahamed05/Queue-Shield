package com.queueshield.notificationservice.event;

import java.time.Instant;

public record IncidentPrioritizedEvent(String eventId, Long incidentId, double score, String tier, Instant computedAt, Instant occurredAt) {
}

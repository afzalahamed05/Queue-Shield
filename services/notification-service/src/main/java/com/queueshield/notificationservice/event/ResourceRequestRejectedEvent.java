package com.queueshield.notificationservice.event;

import java.time.Instant;

public record ResourceRequestRejectedEvent(String eventId, Long assignmentId, Long resourceId, Long incidentId,
                                            String reason, Instant occurredAt) {
}

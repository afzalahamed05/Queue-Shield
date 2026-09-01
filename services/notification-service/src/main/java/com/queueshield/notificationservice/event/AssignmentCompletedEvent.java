package com.queueshield.notificationservice.event;

import java.time.Instant;

public record AssignmentCompletedEvent(String eventId, Long assignmentId, Long incidentId, Long responderId,
                                        Long resourceId, Long shelterId, Instant occurredAt) {
}

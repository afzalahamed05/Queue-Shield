package com.queueshield.assignment.dto;

import com.queueshield.assignment.AssignmentStatus;

import java.time.Instant;

public record AssignmentResponse(
        Long id,
        Long incidentId,
        String incidentTitle,
        Long responderId,
        String responderName,
        Long resourceId,
        String resourceName,
        Long shelterId,
        String shelterName,
        AssignmentStatus status,
        String notes,
        Instant assignedAt
) {
}

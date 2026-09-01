package com.queueshield.assignment.dto;

import com.queueshield.assignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * At least one of responderId/resourceId/shelterId must be provided — validated in
 * {@code AssignmentService} since it spans multiple fields.
 */
public record AssignmentRequest(

        @NotNull(message = "incidentId is required")
        Long incidentId,

        Long responderId,

        Long resourceId,

        Long shelterId,

        AssignmentStatus status,

        @Size(max = 1000, message = "notes must be at most 1000 characters")
        String notes
) {
}

package com.queueshield.assignmentservice.assignment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignmentRequest(
        @NotNull(message = "incidentId is required") Long incidentId,
        Long responderId,
        Long resourceId,
        Long shelterId,
        @Size(max = 1000) String notes
) {
}

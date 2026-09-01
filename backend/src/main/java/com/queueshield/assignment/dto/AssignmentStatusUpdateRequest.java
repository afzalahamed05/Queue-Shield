package com.queueshield.assignment.dto;

import com.queueshield.assignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record AssignmentStatusUpdateRequest(
        @NotNull(message = "status is required")
        AssignmentStatus status
) {
}

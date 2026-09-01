package com.queueshield.assignmentservice.assignment.dto;

import com.queueshield.assignmentservice.assignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record AssignmentStatusUpdateRequest(@NotNull(message = "status is required") AssignmentStatus status) {
}

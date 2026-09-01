package com.queueshield.responderservice.responder.dto;

import jakarta.validation.constraints.NotNull;

public record DispatchRequest(
        @NotNull(message = "assignmentId is required") Long assignmentId,
        @NotNull(message = "incidentId is required") Long incidentId
) {
}

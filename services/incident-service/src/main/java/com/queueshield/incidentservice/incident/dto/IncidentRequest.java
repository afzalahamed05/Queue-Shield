package com.queueshield.incidentservice.incident.dto;

import com.queueshield.incidentservice.incident.IncidentStatus;
import com.queueshield.incidentservice.incident.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentRequest(

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        @NotBlank(message = "location is required")
        @Size(max = 300, message = "location must be at most 300 characters")
        String location,

        @NotNull(message = "severity is required")
        Severity severity,

        IncidentStatus status,

        @Min(value = 0, message = "peopleAffected cannot be negative")
        int peopleAffected,

        @Min(value = 0, message = "vulnerablePopulationCount cannot be negative")
        int vulnerablePopulationCount
) {
}

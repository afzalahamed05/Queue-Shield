package com.queueshield.incident.dto;

import com.queueshield.incident.IncidentStatus;
import com.queueshield.priority.PriorityTier;
import com.queueshield.priority.Severity;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        String location,
        Severity severity,
        IncidentStatus status,
        int peopleAffected,
        int vulnerablePopulationCount,
        Instant reportedAt,
        Instant updatedAt,
        double priorityScore,
        PriorityTier priorityTier,
        PriorityBreakdown priorityBreakdown,
        int assignmentCount
) {
}

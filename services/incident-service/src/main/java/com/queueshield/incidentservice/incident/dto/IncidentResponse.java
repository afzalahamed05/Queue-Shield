package com.queueshield.incidentservice.incident.dto;

import com.queueshield.incidentservice.incident.IncidentStatus;
import com.queueshield.incidentservice.incident.PriorityTier;
import com.queueshield.incidentservice.incident.Severity;

import java.time.Instant;

/**
 * {@code priorityScore}/{@code priorityTier} reflect the last value received from
 * priority-service, not a live computation - see {@link com.queueshield.incidentservice.incident.Incident}.
 * They are {@code null} until the first {@code IncidentPrioritized} event arrives (normally a
 * few hundred milliseconds after creation).
 */
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
        Double priorityScore,
        PriorityTier priorityTier,
        Instant priorityComputedAt
) {
}

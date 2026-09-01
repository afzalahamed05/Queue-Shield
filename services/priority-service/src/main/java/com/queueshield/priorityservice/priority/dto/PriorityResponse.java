package com.queueshield.priorityservice.priority.dto;

import com.queueshield.priorityservice.priority.PriorityTier;

import java.time.Instant;

public record PriorityResponse(
        Long incidentId,
        double score,
        PriorityTier tier,
        double severityComponent,
        double peopleAffectedComponent,
        double vulnerabilityComponent,
        double urgencyComponent,
        double resourceScarcityComponent,
        Instant computedAt
) {
}

package com.queueshield.priorityservice.priority;

public record PriorityScoreResult(
        double score,
        PriorityTier tier,
        double severityComponent,
        double peopleAffectedComponent,
        double vulnerabilityComponent,
        double urgencyComponent,
        double resourceScarcityComponent
) {
}

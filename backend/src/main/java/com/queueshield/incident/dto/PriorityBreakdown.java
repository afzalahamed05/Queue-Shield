package com.queueshield.incident.dto;

public record PriorityBreakdown(
        double severityComponent,
        double peopleAffectedComponent,
        double vulnerabilityComponent,
        double urgencyComponent,
        double resourceScarcityComponent
) {
}

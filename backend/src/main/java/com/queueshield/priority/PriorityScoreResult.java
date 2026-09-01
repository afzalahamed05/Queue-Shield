package com.queueshield.priority;

/**
 * Result of a priority calculation, including the per-factor breakdown so the
 * score is explainable (an API consumer can see *why* an incident scored the way it did,
 * not just the final number). The same shape works if the scorer is later swapped
 * for an ML/AI-backed implementation.
 */
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

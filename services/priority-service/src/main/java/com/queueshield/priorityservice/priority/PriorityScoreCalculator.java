package com.queueshield.priorityservice.priority;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Unchanged from the Phase 1 monolith's calculator (see backend/.../priority/PriorityScoreCalculator.java)
 * - the algorithm itself didn't need to change when this became its own service, only where it
 * gets its inputs from (an incoming Kafka event instead of a direct method call, and an HTTP call
 * to resource-service instead of an injected repository).
 *
 * <p>Five factors, each normalized to a 0-100 sub-score, combined with fixed weights that sum to 1.0:
 * severity (40%), people affected (20%, sqrt-scaled), vulnerable population ratio (20%),
 * urgency/time-decay (10%), resource scarcity (10%, system-wide).
 */
@Component
public class PriorityScoreCalculator {

    private static final double WEIGHT_SEVERITY = 0.40;
    private static final double WEIGHT_PEOPLE_AFFECTED = 0.20;
    private static final double WEIGHT_VULNERABILITY = 0.20;
    private static final double WEIGHT_URGENCY = 0.10;
    private static final double WEIGHT_RESOURCE_SCARCITY = 0.10;

    private static final double URGENCY_SATURATION_MINUTES = 360.0; // 6 hours
    private static final double PEOPLE_AFFECTED_SATURATION = 100.0;

    public PriorityScoreResult calculate(Severity severity,
                                          int peopleAffected,
                                          int vulnerablePopulationCount,
                                          Instant reportedAt,
                                          Instant now,
                                          boolean unresolved,
                                          double resourceAvailabilityRatio) {
        double severityComponent = severity.getBaseScore();
        double peopleAffectedComponent = peopleAffectedScore(peopleAffected);
        double vulnerabilityComponent = vulnerabilityScore(peopleAffected, vulnerablePopulationCount);
        double urgencyComponent = unresolved ? urgencyScore(reportedAt, now) : 0.0;
        double resourceScarcityComponent = resourceScarcityScore(resourceAvailabilityRatio);

        double score = WEIGHT_SEVERITY * severityComponent
                + WEIGHT_PEOPLE_AFFECTED * peopleAffectedComponent
                + WEIGHT_VULNERABILITY * vulnerabilityComponent
                + WEIGHT_URGENCY * urgencyComponent
                + WEIGHT_RESOURCE_SCARCITY * resourceScarcityComponent;

        score = clamp(score);

        return new PriorityScoreResult(
                round(score), tierFor(score),
                round(severityComponent), round(peopleAffectedComponent), round(vulnerabilityComponent),
                round(urgencyComponent), round(resourceScarcityComponent)
        );
    }

    private double peopleAffectedScore(int peopleAffected) {
        if (peopleAffected <= 0) {
            return 0.0;
        }
        return Math.min(100.0, 10.0 * Math.sqrt(peopleAffected));
    }

    private double vulnerabilityScore(int peopleAffected, int vulnerablePopulationCount) {
        if (peopleAffected <= 0 || vulnerablePopulationCount <= 0) {
            return 0.0;
        }
        double ratio = Math.min(1.0, (double) vulnerablePopulationCount / peopleAffected);
        return ratio * 100.0;
    }

    private double urgencyScore(Instant reportedAt, Instant now) {
        if (reportedAt == null || now == null || !now.isAfter(reportedAt)) {
            return 0.0;
        }
        double minutesElapsed = Duration.between(reportedAt, now).toSeconds() / 60.0;
        return Math.min(100.0, (minutesElapsed / URGENCY_SATURATION_MINUTES) * 100.0);
    }

    private double resourceScarcityScore(double resourceAvailabilityRatio) {
        double ratio = clampRatio(resourceAvailabilityRatio);
        return (1.0 - ratio) * 100.0;
    }

    private double clampRatio(double ratio) {
        if (Double.isNaN(ratio)) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private double clamp(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private PriorityTier tierFor(double score) {
        if (score >= 80.0) return PriorityTier.CRITICAL;
        if (score >= 60.0) return PriorityTier.HIGH;
        if (score >= 35.0) return PriorityTier.MEDIUM;
        return PriorityTier.LOW;
    }
}

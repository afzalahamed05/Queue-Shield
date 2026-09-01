package com.queueshield.priority;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Deterministic, explainable incident-priority scoring.
 *
 * <p>This is intentionally NOT machine learning. It is a transparent weighted formula that a
 * coordinator can audit and challenge, which matters for an emergency system: you need to be able
 * to explain to a human why incident A was ranked above incident B. It is designed so a future
 * AI/ML scoring service can be swapped in behind the same {@link PriorityScoreResult} shape
 * without touching any caller.
 *
 * <p>Five factors, each normalized to a 0-100 sub-score, combined with fixed weights that sum to 1.0:
 * <ul>
 *   <li><b>Severity (40%)</b> - coordinator-assigned {@link Severity}, the strongest single signal.</li>
 *   <li><b>People affected (20%)</b> - scaled with sqrt() so the difference between 5 and 20 people
 *       matters more than the difference between 500 and 800 (diminishing marginal urgency).</li>
 *   <li><b>Vulnerable population (20%)</b> - the *proportion* of affected people who are vulnerable
 *       (children, elderly, medically dependent, disabled), not the raw count, so a small incident
 *       that is entirely vulnerable people ranks appropriately high.</li>
 *   <li><b>Urgency / time decay (10%)</b> - minutes elapsed since the incident was reported while it
 *       remains unresolved. An incident that has been sitting open climbs in priority even if its
 *       severity was initially rated as moderate.</li>
 *   <li><b>Resource scarcity (10%)</b> - system-wide ratio of available vs. total emergency
 *       resources. When the whole system is stretched thin, every open incident becomes relatively
 *       more urgent. A smaller weight because it is a system-wide signal, not incident-specific.</li>
 * </ul>
 */
@Component
public class PriorityScoreCalculator {

    private static final double WEIGHT_SEVERITY = 0.40;
    private static final double WEIGHT_PEOPLE_AFFECTED = 0.20;
    private static final double WEIGHT_VULNERABILITY = 0.20;
    private static final double WEIGHT_URGENCY = 0.10;
    private static final double WEIGHT_RESOURCE_SCARCITY = 0.10;

    /** Minutes of elapsed unresolved time that maps to the maximum urgency sub-score. */
    private static final double URGENCY_SATURATION_MINUTES = 360.0; // 6 hours

    /** People-affected count that maps to the maximum people-affected sub-score. */
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
                round(score),
                tierFor(score),
                round(severityComponent),
                round(peopleAffectedComponent),
                round(vulnerabilityComponent),
                round(urgencyComponent),
                round(resourceScarcityComponent)
        );
    }

    private double peopleAffectedScore(int peopleAffected) {
        if (peopleAffected <= 0) {
            return 0.0;
        }
        double scaled = 10.0 * Math.sqrt(peopleAffected);
        return Math.min(100.0, scaled);
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
            return 1.0; // no resource data yet -> treat as neutral (no scarcity penalty)
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
        if (score >= 80.0) {
            return PriorityTier.CRITICAL;
        }
        if (score >= 60.0) {
            return PriorityTier.HIGH;
        }
        if (score >= 35.0) {
            return PriorityTier.MEDIUM;
        }
        return PriorityTier.LOW;
    }
}

package com.queueshield.priority;

/**
 * Coordinator-assigned severity at the time an incident is reported.
 * Each level carries a fixed base score (0-100) that anchors the priority calculation
 * in {@link PriorityScoreCalculator} — severity is the single strongest signal we have,
 * so it gets the largest weight of the five scoring factors.
 */
public enum Severity {
    LOW(10),
    MODERATE(35),
    HIGH(65),
    CRITICAL(95);

    private final int baseScore;

    Severity(int baseScore) {
        this.baseScore = baseScore;
    }

    public int getBaseScore() {
        return baseScore;
    }
}

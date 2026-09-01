package com.queueshield.priorityservice.priority;

/** Deserialized from the {@code severity} field of IncidentEvent JSON - see incident-service's copy of this enum. */
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

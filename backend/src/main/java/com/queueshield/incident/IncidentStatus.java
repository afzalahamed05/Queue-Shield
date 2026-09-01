package com.queueshield.incident;

/**
 * Lifecycle of an incident from first report to closure.
 * REPORTED -> ACKNOWLEDGED -> IN_PROGRESS -> RESOLVED -> CLOSED (CLOSED is a terminal audit state
 * reached after a resolved incident has been reviewed; RESOLVED/CLOSED both stop urgency accrual).
 */
public enum IncidentStatus {
    REPORTED,
    ACKNOWLEDGED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean isUnresolved() {
        return this != RESOLVED && this != CLOSED;
    }
}

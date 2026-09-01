package com.queueshield.incidentservice.incident;

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

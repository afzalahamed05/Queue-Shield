package com.queueshield.incidentservice.incident;

/**
 * Mirrors priority-service's tier values. incident-service never computes this - it only ever
 * stores whatever priority-service last published in an {@code IncidentPrioritized} event.
 */
public enum PriorityTier {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

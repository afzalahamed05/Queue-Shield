package com.queueshield.incidentservice.incident;

/**
 * Coordinator-assigned severity. This enum is intentionally duplicated (identical string values)
 * in priority-service: each service owns its own copy of concepts it needs rather than sharing a
 * library, so the two can evolve/deploy independently. The wire format (event JSON) is the
 * actual contract between them, not a shared Java type.
 */
public enum Severity {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

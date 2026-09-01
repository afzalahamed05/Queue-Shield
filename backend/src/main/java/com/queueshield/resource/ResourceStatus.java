package com.queueshield.resource;

/**
 * Derived from the available/total quantity ratio; kept as a stored, queryable field
 * rather than computed-on-read so it can be filtered/indexed cheaply.
 */
public enum ResourceStatus {
    AVAILABLE,
    LOW,
    DEPLETED,
    OUT_OF_SERVICE
}

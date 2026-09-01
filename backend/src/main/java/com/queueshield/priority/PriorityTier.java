package com.queueshield.priority;

/**
 * Bucketed priority derived from the continuous 0-100 {@link PriorityScoreResult#score()}.
 * Coordinators triage by tier; the score gives fine-grained ordering within a tier.
 */
public enum PriorityTier {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

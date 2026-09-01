package com.queueshield.incident;

import com.queueshield.priority.PriorityTier;
import com.queueshield.priority.Severity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable, optional filters for GET /api/incidents. Each method returns {@code null} when the
 * filter value is null, and {@link Specification#allOf} skips null specs — so callers can chain
 * filters without a chain of manual if-statements.
 */
public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> withStatus(IncidentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Incident> withSeverity(Severity severity) {
        if (severity == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("severity"), severity);
    }

    public static Specification<Incident> withPriorityTier(PriorityTier tier) {
        if (tier == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("priorityTier"), tier);
    }
}

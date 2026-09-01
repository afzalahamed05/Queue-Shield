package com.queueshield.incidentservice.incident;

import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> withStatus(IncidentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Incident> withSeverity(Severity severity) {
        return (root, query, cb) -> cb.equal(root.get("severity"), severity);
    }

    public static Specification<Incident> withPriorityTier(PriorityTier tier) {
        return (root, query, cb) -> cb.equal(root.get("priorityTier"), tier);
    }
}

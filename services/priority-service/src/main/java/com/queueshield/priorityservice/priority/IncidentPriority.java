package com.queueshield.priorityservice.priority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * The durable source of truth for an incident's priority (Postgres). Redis holds a read-through
 * copy of this row for fast lookups - see PriorityCacheService. {@code sourceEventOccurredAt} is
 * the ordering guard: an incoming IncidentCreated/IncidentUpdated is only applied if it's newer
 * than whatever produced the currently stored row, so a redelivered/delayed older event can't
 * regress an already-applied newer computation.
 */
@Entity
@Table(name = "incident_priorities", uniqueConstraints = @UniqueConstraint(columnNames = "incidentId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long incidentId;

    @Column(nullable = false)
    private double score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorityTier tier;

    private double severityComponent;
    private double peopleAffectedComponent;
    private double vulnerabilityComponent;
    private double urgencyComponent;
    private double resourceScarcityComponent;

    @Column(nullable = false)
    private Instant computedAt;

    /** occurredAt of the IncidentCreated/IncidentUpdated event that produced this row. */
    @Column(nullable = false)
    private Instant sourceEventOccurredAt;

    public boolean isNewerThan(Instant candidateEventOccurredAt) {
        return this.sourceEventOccurredAt != null && !candidateEventOccurredAt.isAfter(this.sourceEventOccurredAt);
    }
}

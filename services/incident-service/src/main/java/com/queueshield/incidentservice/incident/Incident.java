package com.queueshield.incidentservice.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Note what is NOT here compared to the Phase 1 monolith's Incident: no {@code assignments}
 * collection. assignment-service owns Assignment records and refers back to this incident only
 * by {@code incidentId} - there is no cross-service JPA relationship, because there is no
 * cross-service database to join across.
 *
 * <p>{@code priorityScore}/{@code priorityTier} are a read-through cache: this service never
 * computes them, it only stores the last value priority-service published via
 * {@code IncidentPrioritized}. {@code priorityUpdatedAt} guards against an out-of-order event
 * (e.g. redelivered after a newer one already applied) regressing the score - a consumer only
 * applies an event if it is newer than what is already stored.
 */
@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 300)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Column(nullable = false)
    private int peopleAffected;

    @Column(nullable = false)
    private int vulnerablePopulationCount;

    @Column(nullable = false)
    private Instant reportedAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PriorityTier priorityTier;

    /** When the currently-stored priority was computed by priority-service (not when we received it). */
    private Instant priorityComputedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.reportedAt = this.reportedAt == null ? now : this.reportedAt;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = IncidentStatus.REPORTED;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void applyPriorityIfNewer(double score, PriorityTier tier, Instant computedAt) {
        if (this.priorityComputedAt == null || computedAt.isAfter(this.priorityComputedAt)) {
            this.priorityScore = score;
            this.priorityTier = tier;
            this.priorityComputedAt = computedAt;
        }
    }
}

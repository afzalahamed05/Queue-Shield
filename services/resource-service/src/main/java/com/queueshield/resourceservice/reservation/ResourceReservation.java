package com.queueshield.resourceservice.reservation;

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
 * The idempotency record for the ResourceRequested -> ResourceAssigned/Rejected saga.
 * {@code assignmentId} is unique: one assignment requests a resource reservation at most once in
 * its lifetime, so it's the natural business key to dedupe on. Kafka gives at-least-once
 * delivery, and reserving a resource is NOT naturally idempotent (replaying "decrement by 1"
 * twice double-decrements) - unlike incident-service's priority cache (which is idempotent by
 * construction), this needs an explicit "have I already handled this?" check backed by a unique
 * DB constraint as the final safety net against a race between two redelivery attempts.
 */
@Entity
@Table(name = "resource_reservations", uniqueConstraints = @UniqueConstraint(columnNames = "assignmentId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assignmentId;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false)
    private Long incidentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(length = 300)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant processedAt;
}

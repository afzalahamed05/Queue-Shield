package com.queueshield.assignmentservice.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * References incident/responder/resource/shelter by plain {@code Long} id only - no JPA
 * {@code @ManyToOne}, because there is no cross-service database to join across. Each of those
 * ids is meaningful only in another service's database; this service just remembers which ones
 * it dispatched together.
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long incidentId;

    private Long responderId;

    private Long resourceId;

    private Long shelterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceRequestStatus resourceRequestStatus;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Instant assignedAt;

    public boolean isActive() {
        return status == AssignmentStatus.PENDING || status == AssignmentStatus.EN_ROUTE || status == AssignmentStatus.ON_SITE;
    }
}

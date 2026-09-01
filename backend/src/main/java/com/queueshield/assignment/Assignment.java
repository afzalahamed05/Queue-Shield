package com.queueshield.assignment;

import com.queueshield.incident.Incident;
import com.queueshield.resource.Resource;
import com.queueshield.responder.Responder;
import com.queueshield.shelter.Shelter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A dispatch record: some combination of a responder, a resource, and/or a shelter tasked
 * against one incident. At least one of responder/resource/shelter must be present — enforced
 * in {@code AssignmentService}, not here, since it is a cross-field business rule rather than
 * something a single-field bean-validation annotation can express cleanly.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id")
    private Responder responder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Instant assignedAt;

    @PrePersist
    void onCreate() {
        this.assignedAt = this.assignedAt == null ? Instant.now() : this.assignedAt;
        if (this.status == null) {
            this.status = AssignmentStatus.PENDING;
        }
    }
}

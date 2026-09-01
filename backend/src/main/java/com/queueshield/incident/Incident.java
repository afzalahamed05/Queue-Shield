package com.queueshield.incident;

import com.queueshield.assignment.Assignment;
import com.queueshield.priority.PriorityTier;
import com.queueshield.priority.Severity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorityTier priorityTier;

    @Builder.Default
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

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
}

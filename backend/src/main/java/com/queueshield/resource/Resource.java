package com.queueshield.resource;

import com.queueshield.assignment.Assignment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceType type;

    @Column(nullable = false)
    private int quantityTotal;

    @Column(nullable = false)
    private int quantityAvailable;

    @Column(nullable = false, length = 300)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    /**
     * Keeps {@code status} consistent with the quantity fields. Called by the service layer
     * after any quantity mutation instead of on every getter, so status changes are explicit
     * and visible in persistence history.
     */
    public void recomputeStatus() {
        if (this.status == ResourceStatus.OUT_OF_SERVICE) {
            return;
        }
        if (quantityAvailable <= 0) {
            this.status = ResourceStatus.DEPLETED;
        } else if (quantityTotal > 0 && quantityAvailable < quantityTotal * 0.2) {
            this.status = ResourceStatus.LOW;
        } else {
            this.status = ResourceStatus.AVAILABLE;
        }
    }
}

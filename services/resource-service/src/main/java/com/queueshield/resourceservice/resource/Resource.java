package com.queueshield.resourceservice.resource;

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

/** No {@code assignments} collection here (unlike the Phase 1 monolith) - assignment-service owns that reference now. */
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

    /** @return true if a unit was reserved, false if none were available. */
    public boolean tryReserveOneUnit() {
        if (quantityAvailable <= 0) {
            return false;
        }
        quantityAvailable--;
        recomputeStatus();
        return true;
    }

    public void releaseOneUnit() {
        quantityAvailable = Math.min(quantityTotal, quantityAvailable + 1);
        recomputeStatus();
    }
}

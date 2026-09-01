package com.queueshield.resourceservice.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceReservationRepository extends JpaRepository<ResourceReservation, Long> {
    Optional<ResourceReservation> findByAssignmentId(Long assignmentId);
}

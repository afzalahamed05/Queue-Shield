package com.queueshield.priorityservice.priority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentPriorityRepository extends JpaRepository<IncidentPriority, Long> {
    Optional<IncidentPriority> findByIncidentId(Long incidentId);
}

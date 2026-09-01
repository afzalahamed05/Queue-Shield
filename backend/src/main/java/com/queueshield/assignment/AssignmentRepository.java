package com.queueshield.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByIncidentId(Long incidentId, Pageable pageable);

    Page<Assignment> findByStatus(AssignmentStatus status, Pageable pageable);

    Page<Assignment> findByResponderId(Long responderId, Pageable pageable);

    Page<Assignment> findByResourceId(Long resourceId, Pageable pageable);

    Page<Assignment> findByShelterId(Long shelterId, Pageable pageable);
}

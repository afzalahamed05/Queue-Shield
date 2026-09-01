package com.queueshield.incident;

import com.queueshield.priority.PriorityTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * {@link JpaSpecificationExecutor} lets {@link IncidentService} compose optional filters
 * (status, severity, priority tier) at query time instead of us hand-writing a repository
 * method for every combination.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByPriorityTierAndStatusNotIn(PriorityTier priorityTier, Collection<IncidentStatus> excludedStatuses);

    long countByStatusNotIn(Collection<IncidentStatus> excludedStatuses);
}

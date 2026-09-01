package com.queueshield.incidentservice.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByPriorityTierAndStatusNotIn(PriorityTier priorityTier, Collection<IncidentStatus> excludedStatuses);

    long countByStatusNotIn(Collection<IncidentStatus> excludedStatuses);
}

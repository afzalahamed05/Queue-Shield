package com.queueshield.incidentservice.incident;

import com.queueshield.incidentservice.common.exception.ResourceNotFoundException;
import com.queueshield.incidentservice.event.IncidentEventProducer;
import com.queueshield.incidentservice.incident.dto.IncidentMapper;
import com.queueshield.incidentservice.incident.dto.IncidentRequest;
import com.queueshield.incidentservice.incident.dto.IncidentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notice what's gone compared to the Phase 1 monolith's IncidentService: no
 * PriorityScoreCalculator, no ResourceRepository dependency. This service's only job now is
 * owning the incident record and telling the world (via Kafka) when it changes -
 * priority-service reacts to that independently.
 */
@Service
@Transactional
public class IncidentService {

    private static final List<IncidentStatus> RESOLVED_STATUSES = List.of(IncidentStatus.RESOLVED, IncidentStatus.CLOSED);

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final IncidentEventProducer eventProducer;

    public IncidentService(IncidentRepository incidentRepository, IncidentMapper incidentMapper,
                            IncidentEventProducer eventProducer) {
        this.incidentRepository = incidentRepository;
        this.incidentMapper = incidentMapper;
        this.eventProducer = eventProducer;
    }

    public IncidentResponse create(@Valid IncidentRequest request) {
        validateVulnerableCount(request);

        Incident incident = new Incident();
        incidentMapper.applyToEntity(request, incident);
        if (incident.getStatus() == null) {
            incident.setStatus(IncidentStatus.REPORTED);
        }

        Incident saved = incidentRepository.save(incident);
        eventProducer.publishCreated(saved);
        return incidentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        return incidentMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> list(IncidentStatus status, Severity severity, PriorityTier priorityTier, Pageable pageable) {
        Specification<Incident> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and(IncidentSpecifications.withStatus(status));
        }
        if (severity != null) {
            spec = spec.and(IncidentSpecifications.withSeverity(severity));
        }
        if (priorityTier != null) {
            spec = spec.and(IncidentSpecifications.withPriorityTier(priorityTier));
        }
        return incidentRepository.findAll(spec, pageable).map(incidentMapper::toResponse);
    }

    public IncidentResponse update(Long id, @Valid IncidentRequest request) {
        validateVulnerableCount(request);
        Incident incident = findOrThrow(id);
        incidentMapper.applyToEntity(request, incident);

        Incident saved = incidentRepository.save(incident);
        eventProducer.publishUpdated(saved);
        return incidentMapper.toResponse(saved);
    }

    /** Re-publishes the current state so priority-service recomputes - e.g. after a coordinator suspects the score is stale. */
    public IncidentResponse requestReprioritization(Long id) {
        Incident incident = findOrThrow(id);
        eventProducer.publishUpdated(incident);
        return incidentMapper.toResponse(incident);
    }

    public void delete(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Incident", id);
        }
        incidentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countCritical() {
        return incidentRepository.countByPriorityTierAndStatusNotIn(PriorityTier.CRITICAL, RESOLVED_STATUSES);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return incidentRepository.countByStatusNotIn(RESOLVED_STATUSES);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return incidentRepository.count();
    }

    private Incident findOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Incident", id));
    }

    private void validateVulnerableCount(IncidentRequest request) {
        if (request.vulnerablePopulationCount() > request.peopleAffected()) {
            throw new IllegalArgumentException("vulnerablePopulationCount cannot exceed peopleAffected");
        }
    }
}

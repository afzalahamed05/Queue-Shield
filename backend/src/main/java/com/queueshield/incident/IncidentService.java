package com.queueshield.incident;

import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.incident.dto.IncidentMapper;
import com.queueshield.incident.dto.IncidentRequest;
import com.queueshield.incident.dto.IncidentResponse;
import com.queueshield.priority.PriorityScoreCalculator;
import com.queueshield.priority.PriorityScoreResult;
import com.queueshield.priority.PriorityTier;
import com.queueshield.resource.ResourceRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class IncidentService {

    private static final List<IncidentStatus> RESOLVED_STATUSES = List.of(IncidentStatus.RESOLVED, IncidentStatus.CLOSED);

    private final IncidentRepository incidentRepository;
    private final ResourceRepository resourceRepository;
    private final PriorityScoreCalculator priorityScoreCalculator;
    private final IncidentMapper incidentMapper;

    public IncidentService(IncidentRepository incidentRepository,
                            ResourceRepository resourceRepository,
                            PriorityScoreCalculator priorityScoreCalculator,
                            IncidentMapper incidentMapper) {
        this.incidentRepository = incidentRepository;
        this.resourceRepository = resourceRepository;
        this.priorityScoreCalculator = priorityScoreCalculator;
        this.incidentMapper = incidentMapper;
    }

    public IncidentResponse create(@Valid IncidentRequest request) {
        validateVulnerableCount(request);

        Incident incident = new Incident();
        incidentMapper.applyToEntity(request, incident);
        if (incident.getStatus() == null) {
            incident.setStatus(IncidentStatus.REPORTED);
        }

        PriorityScoreResult liveScore = computeLiveScore(incident, Instant.now());
        incident.setPriorityScore(liveScore.score());
        incident.setPriorityTier(liveScore.tier());

        Incident saved = incidentRepository.save(incident);
        return incidentMapper.toResponse(saved, liveScore);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        Incident incident = findOrThrow(id);
        PriorityScoreResult liveScore = computeLiveScore(incident, Instant.now());
        return incidentMapper.toResponse(incident, liveScore);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> list(IncidentStatus status, com.queueshield.priority.Severity severity,
                                        PriorityTier priorityTier, Pageable pageable) {
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
        Instant now = Instant.now();
        return incidentRepository.findAll(spec, pageable)
                .map(incident -> incidentMapper.toResponse(incident, computeLiveScore(incident, now)));
    }

    public IncidentResponse update(Long id, @Valid IncidentRequest request) {
        validateVulnerableCount(request);
        Incident incident = findOrThrow(id);
        incidentMapper.applyToEntity(request, incident);

        PriorityScoreResult liveScore = computeLiveScore(incident, Instant.now());
        incident.setPriorityScore(liveScore.score());
        incident.setPriorityTier(liveScore.tier());

        Incident saved = incidentRepository.save(incident);
        return incidentMapper.toResponse(saved, liveScore);
    }

    public IncidentResponse recalculatePriority(Long id) {
        Incident incident = findOrThrow(id);
        PriorityScoreResult liveScore = computeLiveScore(incident, Instant.now());
        incident.setPriorityScore(liveScore.score());
        incident.setPriorityTier(liveScore.tier());
        Incident saved = incidentRepository.save(incident);
        return incidentMapper.toResponse(saved, liveScore);
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

    /**
     * Recomputes the priority score for an incident against the current wall-clock time and the
     * current system-wide resource availability ratio. Pure with respect to persistence - does
     * not save anything, callers decide whether to persist the result.
     */
    PriorityScoreResult computeLiveScore(Incident incident, Instant now) {
        double resourceRatio = currentResourceAvailabilityRatio();
        return priorityScoreCalculator.calculate(
                incident.getSeverity(),
                incident.getPeopleAffected(),
                incident.getVulnerablePopulationCount(),
                incident.getReportedAt() != null ? incident.getReportedAt() : now,
                now,
                incident.getStatus() == null || incident.getStatus().isUnresolved(),
                resourceRatio
        );
    }

    private double currentResourceAvailabilityRatio() {
        long total = resourceRepository.sumQuantityTotal();
        if (total <= 0) {
            return Double.NaN; // no resource data yet -> calculator treats this as neutral
        }
        long available = resourceRepository.sumQuantityAvailable();
        return (double) available / (double) total;
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

package com.queueshield.priorityservice.priority;

import com.queueshield.priorityservice.event.IncidentEvent;
import com.queueshield.priorityservice.event.IncidentPrioritizedProducer;
import com.queueshield.priorityservice.resource.ResourceAvailabilityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
public class PriorityCalculationService {

    private final IncidentPriorityRepository repository;
    private final PriorityScoreCalculator calculator;
    private final ResourceAvailabilityClient resourceAvailabilityClient;
    private final IncidentPrioritizedProducer producer;
    private final PriorityCacheService cacheService;

    public PriorityCalculationService(IncidentPriorityRepository repository,
                                       PriorityScoreCalculator calculator,
                                       ResourceAvailabilityClient resourceAvailabilityClient,
                                       IncidentPrioritizedProducer producer,
                                       PriorityCacheService cacheService) {
        this.repository = repository;
        this.calculator = calculator;
        this.resourceAvailabilityClient = resourceAvailabilityClient;
        this.producer = producer;
        this.cacheService = cacheService;
    }

    @Transactional
    public void recompute(IncidentEvent event) {
        IncidentPriority existing = repository.findByIncidentId(event.incidentId()).orElse(null);
        if (existing != null && existing.isNewerThan(event.occurredAt())) {
            log.info("Ignoring stale/redelivered event for incident {} (event occurredAt={} <= stored sourceEventOccurredAt={})",
                    event.incidentId(), event.occurredAt(), existing.getSourceEventOccurredAt());
            return;
        }

        Severity severity = Severity.valueOf(event.severity());
        boolean unresolved = !"RESOLVED".equals(event.status()) && !"CLOSED".equals(event.status());
        double resourceRatio = resourceAvailabilityClient.getAvailabilityRatio();
        Instant now = Instant.now();

        PriorityScoreResult result = calculator.calculate(
                severity, event.peopleAffected(), event.vulnerablePopulationCount(),
                event.reportedAt(), now, unresolved, resourceRatio);

        IncidentPriority priority = existing != null ? existing : new IncidentPriority();
        priority.setIncidentId(event.incidentId());
        priority.setScore(result.score());
        priority.setTier(result.tier());
        priority.setSeverityComponent(result.severityComponent());
        priority.setPeopleAffectedComponent(result.peopleAffectedComponent());
        priority.setVulnerabilityComponent(result.vulnerabilityComponent());
        priority.setUrgencyComponent(result.urgencyComponent());
        priority.setResourceScarcityComponent(result.resourceScarcityComponent());
        priority.setComputedAt(now);
        priority.setSourceEventOccurredAt(event.occurredAt());

        IncidentPriority saved = repository.save(priority);
        cacheService.put(saved);
        producer.publish(saved);

        log.info("Computed priority for incident {}: score={} tier={}", event.incidentId(), result.score(), result.tier());
    }
}

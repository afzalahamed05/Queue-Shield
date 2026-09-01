package com.queueshield.priorityservice.priority;

import com.queueshield.priorityservice.common.exception.ResourceNotFoundException;
import com.queueshield.priorityservice.priority.dto.PriorityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/priorities")
@Tag(name = "Priorities", description = "Read-only access to computed incident priority scores")
public class PriorityController {

    private final IncidentPriorityRepository repository;
    private final PriorityCacheService cacheService;

    public PriorityController(IncidentPriorityRepository repository, PriorityCacheService cacheService) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @GetMapping("/{incidentId}")
    @Operation(summary = "Cache-aside read: tries Redis first, falls back to Postgres and repopulates the cache on a miss")
    public PriorityResponse getByIncidentId(@PathVariable Long incidentId) {
        return cacheService.get(incidentId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    IncidentPriority priority = repository.findByIncidentId(incidentId)
                            .orElseThrow(() -> ResourceNotFoundException.forEntity("Priority for incident", incidentId));
                    cacheService.put(priority);
                    return toResponse(priority);
                });
    }

    private PriorityResponse toResponse(PriorityCacheService.CachedPriority cached) {
        return new PriorityResponse(cached.incidentId(), cached.score(), cached.tier(),
                cached.severityComponent(), cached.peopleAffectedComponent(), cached.vulnerabilityComponent(),
                cached.urgencyComponent(), cached.resourceScarcityComponent(), cached.computedAt());
    }

    private PriorityResponse toResponse(IncidentPriority p) {
        return new PriorityResponse(p.getIncidentId(), p.getScore(), p.getTier(),
                p.getSeverityComponent(), p.getPeopleAffectedComponent(), p.getVulnerabilityComponent(),
                p.getUrgencyComponent(), p.getResourceScarcityComponent(), p.getComputedAt());
    }
}

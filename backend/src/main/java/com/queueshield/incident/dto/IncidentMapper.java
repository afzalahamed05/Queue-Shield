package com.queueshield.incident.dto;

import com.queueshield.incident.Incident;
import com.queueshield.priority.PriorityScoreResult;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Incident} entity and its API DTOs. The priority score/tier shown in
 * a response is always the *live* recalculation (see {@code IncidentService#computeLiveScore})
 * passed in as {@code liveScore}, not necessarily the value last persisted on the entity — the
 * urgency component decays over time even between writes, so a GET should always show the
 * freshest number.
 */
@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident, PriorityScoreResult liveScore) {
        PriorityBreakdown breakdown = new PriorityBreakdown(
                liveScore.severityComponent(),
                liveScore.peopleAffectedComponent(),
                liveScore.vulnerabilityComponent(),
                liveScore.urgencyComponent(),
                liveScore.resourceScarcityComponent()
        );
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getLocation(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getPeopleAffected(),
                incident.getVulnerablePopulationCount(),
                incident.getReportedAt(),
                incident.getUpdatedAt(),
                liveScore.score(),
                liveScore.tier(),
                breakdown,
                incident.getAssignments() == null ? 0 : incident.getAssignments().size()
        );
    }

    public void applyToEntity(IncidentRequest request, Incident target) {
        target.setTitle(request.title());
        target.setDescription(request.description());
        target.setLocation(request.location());
        target.setSeverity(request.severity());
        target.setPeopleAffected(request.peopleAffected());
        target.setVulnerablePopulationCount(request.vulnerablePopulationCount());
        if (request.status() != null) {
            target.setStatus(request.status());
        }
    }
}

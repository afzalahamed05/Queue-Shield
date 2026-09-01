package com.queueshield.incidentservice.incident.dto;

import com.queueshield.incidentservice.incident.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident) {
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
                incident.getPriorityScore(),
                incident.getPriorityTier(),
                incident.getPriorityComputedAt()
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

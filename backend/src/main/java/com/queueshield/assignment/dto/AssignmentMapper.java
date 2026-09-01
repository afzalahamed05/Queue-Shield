package com.queueshield.assignment.dto;

import com.queueshield.assignment.Assignment;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getIncident().getId(),
                assignment.getIncident().getTitle(),
                assignment.getResponder() != null ? assignment.getResponder().getId() : null,
                assignment.getResponder() != null ? assignment.getResponder().getName() : null,
                assignment.getResource() != null ? assignment.getResource().getId() : null,
                assignment.getResource() != null ? assignment.getResource().getName() : null,
                assignment.getShelter() != null ? assignment.getShelter().getId() : null,
                assignment.getShelter() != null ? assignment.getShelter().getName() : null,
                assignment.getStatus(),
                assignment.getNotes(),
                assignment.getAssignedAt()
        );
    }
}

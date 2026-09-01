package com.queueshield.assignmentservice.assignment.dto;

import com.queueshield.assignmentservice.assignment.Assignment;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(assignment.getId(), assignment.getIncidentId(), assignment.getResponderId(),
                assignment.getResourceId(), assignment.getShelterId(), assignment.getStatus(),
                assignment.getResourceRequestStatus(), assignment.getNotes(), assignment.getAssignedAt());
    }
}

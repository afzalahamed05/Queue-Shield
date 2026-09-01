package com.queueshield.assignmentservice.assignment.dto;

import com.queueshield.assignmentservice.assignment.AssignmentStatus;
import com.queueshield.assignmentservice.assignment.ResourceRequestStatus;

import java.time.Instant;

/**
 * Only ids, not denormalized names (Phase 1's monolith could join to Incident/Responder/Resource
 * for a display-friendly title/name; this service can't, since those live in other services'
 * databases now). Enriching ids into display names is the API gateway/frontend's job - see the
 * README's API boundaries section.
 */
public record AssignmentResponse(
        Long id,
        Long incidentId,
        Long responderId,
        Long resourceId,
        Long shelterId,
        AssignmentStatus status,
        ResourceRequestStatus resourceRequestStatus,
        String notes,
        Instant assignedAt
) {
}

package com.queueshield.assignmentservice.assignment;

/** Tracks the async resource-reservation saga's outcome independently of the coordinator-facing {@link AssignmentStatus}. */
public enum ResourceRequestStatus {
    NOT_REQUESTED,
    PENDING,
    ASSIGNED,
    REJECTED
}

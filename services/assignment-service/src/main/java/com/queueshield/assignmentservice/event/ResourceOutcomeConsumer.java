package com.queueshield.assignmentservice.event;

import com.queueshield.assignmentservice.assignment.Assignment;
import com.queueshield.assignmentservice.assignment.AssignmentRepository;
import com.queueshield.assignmentservice.assignment.ResourceRequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent by state-check rather than a dedup table: {@code resourceRequestStatus} only ever
 * transitions PENDING -> ASSIGNED or PENDING -> REJECTED once, so a redelivered duplicate of
 * either event finds the assignment already past PENDING and is a safe no-op. This is a third
 * idempotency technique in this system (alongside incident-service's timestamp-ordering guard and
 * resource-service's dedup table) - the right one depends on what "processing twice" would
 * actually do to the data; here it's a one-way state transition, so checking the current state is
 * sufficient.
 */
@Component
@Slf4j
public class ResourceOutcomeConsumer {

    private final AssignmentRepository assignmentRepository;

    public ResourceOutcomeConsumer(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @KafkaListener(topics = Topics.RESOURCE_ASSIGNED, groupId = "assignment-service")
    @Transactional
    public void onResourceAssigned(ResourceAssignedEvent event) {
        applyOutcome(event.assignmentId(), ResourceRequestStatus.ASSIGNED, null);
    }

    @KafkaListener(topics = Topics.RESOURCE_REQUEST_REJECTED, groupId = "assignment-service")
    @Transactional
    public void onResourceRequestRejected(ResourceRequestRejectedEvent event) {
        applyOutcome(event.assignmentId(), ResourceRequestStatus.REJECTED, event.reason());
    }

    private void applyOutcome(Long assignmentId, ResourceRequestStatus outcome, String reason) {
        assignmentRepository.findById(assignmentId).ifPresentOrElse(assignment -> {
            if (assignment.getResourceRequestStatus() != ResourceRequestStatus.PENDING) {
                log.info("Assignment {} resource request already resolved ({}) - ignoring duplicate {}",
                        assignmentId, assignment.getResourceRequestStatus(), outcome);
                return;
            }
            assignment.setResourceRequestStatus(outcome);
            if (outcome == ResourceRequestStatus.REJECTED && reason != null) {
                String note = (assignment.getNotes() == null ? "" : assignment.getNotes() + " | ") + "Resource request rejected: " + reason;
                assignment.setNotes(note);
            }
            assignmentRepository.save(assignment);
            log.info("Assignment {} resource request resolved: {}", assignmentId, outcome);
        }, () -> log.warn("Received resource outcome for unknown assignment {} - dropping", assignmentId));
    }
}

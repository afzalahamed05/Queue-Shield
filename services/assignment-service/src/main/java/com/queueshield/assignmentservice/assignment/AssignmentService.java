package com.queueshield.assignmentservice.assignment;

import com.queueshield.assignmentservice.assignment.dto.AssignmentMapper;
import com.queueshield.assignmentservice.assignment.dto.AssignmentRequest;
import com.queueshield.assignmentservice.assignment.dto.AssignmentResponse;
import com.queueshield.assignmentservice.client.ResourceClient;
import com.queueshield.assignmentservice.client.ResponderClient;
import com.queueshield.assignmentservice.common.exception.BusinessRuleViolationException;
import com.queueshield.assignmentservice.common.exception.ResourceNotFoundException;
import com.queueshield.assignmentservice.event.AssignmentCompletedProducer;
import com.queueshield.assignmentservice.event.ResourceRequestProducer;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Deliberately NOT wrapped in one big {@code @Transactional}: this method makes a synchronous
 * HTTP call to responder-service partway through, and holding a DB transaction open across a
 * network hop to another service is exactly the kind of distributed-transaction trap
 * microservices are supposed to avoid. Instead: save the row first (its own transaction, via
 * Spring Data's default), attempt the external call, and explicitly compensate (delete the row)
 * if it fails. That compensating-action pattern - not a two-phase commit - is how consistency is
 * maintained across services here.
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final ResponderClient responderClient;
    private final ResourceClient resourceClient;
    private final ResourceRequestProducer resourceRequestProducer;
    private final AssignmentCompletedProducer assignmentCompletedProducer;

    public AssignmentService(AssignmentRepository assignmentRepository, AssignmentMapper assignmentMapper,
                              ResponderClient responderClient, ResourceClient resourceClient,
                              ResourceRequestProducer resourceRequestProducer,
                              AssignmentCompletedProducer assignmentCompletedProducer) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
        this.responderClient = responderClient;
        this.resourceClient = resourceClient;
        this.resourceRequestProducer = resourceRequestProducer;
        this.assignmentCompletedProducer = assignmentCompletedProducer;
    }

    public AssignmentResponse create(@Valid AssignmentRequest request) {
        validateAtLeastOneTarget(request);

        Assignment assignment = Assignment.builder()
                .incidentId(request.incidentId())
                .responderId(request.responderId())
                .resourceId(request.resourceId())
                .shelterId(request.shelterId())
                .status(AssignmentStatus.PENDING)
                .resourceRequestStatus(request.resourceId() != null ? ResourceRequestStatus.PENDING : ResourceRequestStatus.NOT_REQUESTED)
                .notes(request.notes())
                .assignedAt(Instant.now())
                .build();
        Assignment saved = assignmentRepository.save(assignment);

        if (request.responderId() != null) {
            try {
                responderClient.dispatch(request.responderId(), saved.getId(), request.incidentId());
            } catch (BusinessRuleViolationException ex) {
                assignmentRepository.delete(saved);
                throw ex;
            }
        }

        if (request.resourceId() != null) {
            resourceRequestProducer.publishRequested(saved.getId(), request.resourceId(), request.incidentId());
        }

        return assignmentMapper.toResponse(saved);
    }

    public AssignmentResponse getById(Long id) {
        return assignmentMapper.toResponse(findOrThrow(id));
    }

    public Page<AssignmentResponse> list(Long incidentId, AssignmentStatus status, Pageable pageable) {
        Page<Assignment> page;
        if (incidentId != null) {
            page = assignmentRepository.findByIncidentId(incidentId, pageable);
        } else if (status != null) {
            page = assignmentRepository.findByStatus(status, pageable);
        } else {
            page = assignmentRepository.findAll(pageable);
        }
        return page.map(assignmentMapper::toResponse);
    }

    public AssignmentResponse updateStatus(Long id, AssignmentStatus newStatus) {
        Assignment assignment = findOrThrow(id);
        boolean wasActive = assignment.isActive();
        boolean becomesInactive = !isActive(newStatus);

        if (wasActive && becomesInactive) {
            releaseHeldResources(assignment);
        }

        assignment.setStatus(newStatus);
        Assignment saved = assignmentRepository.save(assignment);

        if (newStatus == AssignmentStatus.COMPLETED) {
            assignmentCompletedProducer.publish(saved);
        }

        return assignmentMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Assignment assignment = findOrThrow(id);
        if (assignment.isActive()) {
            releaseHeldResources(assignment);
        }
        assignmentRepository.delete(assignment);
    }

    private void releaseHeldResources(Assignment assignment) {
        if (assignment.getResponderId() != null) {
            responderClient.release(assignment.getResponderId());
        }
        if (assignment.getResourceId() != null && assignment.getResourceRequestStatus() == ResourceRequestStatus.ASSIGNED) {
            resourceClient.release(assignment.getResourceId());
        }
    }

    private boolean isActive(AssignmentStatus status) {
        return status == AssignmentStatus.PENDING || status == AssignmentStatus.EN_ROUTE || status == AssignmentStatus.ON_SITE;
    }

    private Assignment findOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Assignment", id));
    }

    private void validateAtLeastOneTarget(AssignmentRequest request) {
        if (request.responderId() == null && request.resourceId() == null && request.shelterId() == null) {
            throw new BusinessRuleViolationException(
                    "An assignment must reference at least one of responderId, resourceId, or shelterId");
        }
    }
}

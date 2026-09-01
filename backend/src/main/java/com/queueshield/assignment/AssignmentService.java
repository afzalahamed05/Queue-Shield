package com.queueshield.assignment;

import com.queueshield.assignment.dto.AssignmentMapper;
import com.queueshield.assignment.dto.AssignmentRequest;
import com.queueshield.assignment.dto.AssignmentResponse;
import com.queueshield.common.exception.BusinessRuleViolationException;
import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.incident.Incident;
import com.queueshield.incident.IncidentRepository;
import com.queueshield.resource.Resource;
import com.queueshield.resource.ResourceRepository;
import com.queueshield.responder.Responder;
import com.queueshield.responder.ResponderRepository;
import com.queueshield.responder.ResponderStatus;
import com.queueshield.shelter.Shelter;
import com.queueshield.shelter.ShelterRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dispatch logic. Creating an assignment has real side effects on the resources it references:
 * a dispatched responder moves to {@link ResponderStatus#DISPATCHED} and a committed resource's
 * available quantity is decremented by one unit. Cancelling/completing an assignment reverses
 * those effects. This keeps "how many ambulances do we actually have free right now" trustworthy
 * without a coordinator having to manually update responder/resource records on every dispatch.
 */
@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final IncidentRepository incidentRepository;
    private final ResponderRepository responderRepository;
    private final ResourceRepository resourceRepository;
    private final ShelterRepository shelterRepository;
    private final AssignmentMapper assignmentMapper;

    public AssignmentService(AssignmentRepository assignmentRepository,
                              IncidentRepository incidentRepository,
                              ResponderRepository responderRepository,
                              ResourceRepository resourceRepository,
                              ShelterRepository shelterRepository,
                              AssignmentMapper assignmentMapper) {
        this.assignmentRepository = assignmentRepository;
        this.incidentRepository = incidentRepository;
        this.responderRepository = responderRepository;
        this.resourceRepository = resourceRepository;
        this.shelterRepository = shelterRepository;
        this.assignmentMapper = assignmentMapper;
    }

    public AssignmentResponse create(@Valid AssignmentRequest request) {
        validateAtLeastOneTarget(request);

        Incident incident = incidentRepository.findById(request.incidentId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Incident", request.incidentId()));

        Assignment assignment = new Assignment();
        assignment.setIncident(incident);
        assignment.setNotes(request.notes());
        assignment.setStatus(request.status() != null ? request.status() : AssignmentStatus.PENDING);

        if (request.responderId() != null) {
            Responder responder = responderRepository.findById(request.responderId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Responder", request.responderId()));
            dispatchResponder(responder);
            assignment.setResponder(responder);
        }

        if (request.resourceId() != null) {
            Resource resource = resourceRepository.findById(request.resourceId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Resource", request.resourceId()));
            commitResourceUnit(resource);
            assignment.setResource(resource);
        }

        if (request.shelterId() != null) {
            Shelter shelter = shelterRepository.findById(request.shelterId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Shelter", request.shelterId()));
            assignment.setShelter(shelter);
        }

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getById(Long id) {
        return assignmentMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
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
        boolean wasActive = isActive(assignment.getStatus());
        boolean becomesInactive = !isActive(newStatus);

        if (wasActive && becomesInactive) {
            releaseResponder(assignment.getResponder());
            releaseResourceUnit(assignment.getResource());
        }

        assignment.setStatus(newStatus);
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    public void delete(Long id) {
        Assignment assignment = findOrThrow(id);
        if (isActive(assignment.getStatus())) {
            releaseResponder(assignment.getResponder());
            releaseResourceUnit(assignment.getResource());
        }
        assignmentRepository.delete(assignment);
    }

    private void dispatchResponder(Responder responder) {
        if (responder.getStatus() == ResponderStatus.OFF_DUTY || responder.getStatus() == ResponderStatus.UNAVAILABLE) {
            throw new BusinessRuleViolationException(
                    "Responder " + responder.getId() + " is not available for dispatch (status=" + responder.getStatus() + ")");
        }
        responder.setStatus(ResponderStatus.DISPATCHED);
        responderRepository.save(responder);
    }

    private void releaseResponder(Responder responder) {
        if (responder == null) {
            return;
        }
        responder.setStatus(ResponderStatus.AVAILABLE);
        responderRepository.save(responder);
    }

    private void commitResourceUnit(Resource resource) {
        if (resource.getQuantityAvailable() <= 0) {
            throw new BusinessRuleViolationException("Resource " + resource.getId() + " has no available quantity");
        }
        resource.setQuantityAvailable(resource.getQuantityAvailable() - 1);
        resource.recomputeStatus();
        resourceRepository.save(resource);
    }

    private void releaseResourceUnit(Resource resource) {
        if (resource == null) {
            return;
        }
        resource.setQuantityAvailable(Math.min(resource.getQuantityTotal(), resource.getQuantityAvailable() + 1));
        resource.recomputeStatus();
        resourceRepository.save(resource);
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

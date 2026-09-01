package com.queueshield.responderservice.responder;

import com.queueshield.responderservice.common.exception.BusinessRuleViolationException;
import com.queueshield.responderservice.common.exception.ResourceNotFoundException;
import com.queueshield.responderservice.event.ResponderEventProducer;
import com.queueshield.responderservice.responder.dto.DispatchRequest;
import com.queueshield.responderservice.responder.dto.ResponderMapper;
import com.queueshield.responderservice.responder.dto.ResponderRequest;
import com.queueshield.responderservice.responder.dto.ResponderResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResponderService {

    private final ResponderRepository responderRepository;
    private final ResponderMapper responderMapper;
    private final ResponderAvailabilityCacheService availabilityCacheService;
    private final ResponderEventProducer eventProducer;

    public ResponderService(ResponderRepository responderRepository, ResponderMapper responderMapper,
                             ResponderAvailabilityCacheService availabilityCacheService,
                             ResponderEventProducer eventProducer) {
        this.responderRepository = responderRepository;
        this.responderMapper = responderMapper;
        this.availabilityCacheService = availabilityCacheService;
        this.eventProducer = eventProducer;
    }

    public ResponderResponse create(@Valid ResponderRequest request) {
        Responder responder = new Responder();
        responderMapper.applyToEntity(request, responder);
        Responder saved = responderRepository.save(responder);
        availabilityCacheService.refresh();
        return responderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResponderResponse getById(Long id) {
        return responderMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ResponderResponse> list(ResponderRole role, ResponderStatus status, Pageable pageable) {
        Page<Responder> page;
        if (role != null && status != null) {
            page = responderRepository.findByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            page = responderRepository.findByRole(role, pageable);
        } else if (status != null) {
            page = responderRepository.findByStatus(status, pageable);
        } else {
            page = responderRepository.findAll(pageable);
        }
        return page.map(responderMapper::toResponse);
    }

    public ResponderResponse update(Long id, @Valid ResponderRequest request) {
        Responder responder = findOrThrow(id);
        responderMapper.applyToEntity(request, responder);
        Responder saved = responderRepository.save(responder);
        availabilityCacheService.refresh();
        return responderMapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!responderRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Responder", id);
        }
        responderRepository.deleteById(id);
        availabilityCacheService.refresh();
    }

    /** Synchronous - assignment-service needs an immediate, unambiguous answer to avoid double-dispatching a unit. */
    public ResponderResponse dispatch(Long id, DispatchRequest request) {
        Responder responder = findOrThrow(id);
        if (responder.getStatus() == ResponderStatus.OFF_DUTY || responder.getStatus() == ResponderStatus.UNAVAILABLE) {
            throw new BusinessRuleViolationException(
                    "Responder " + id + " is not available for dispatch (status=" + responder.getStatus() + ")");
        }
        if (responder.getStatus() == ResponderStatus.DISPATCHED) {
            throw new BusinessRuleViolationException("Responder " + id + " is already dispatched");
        }

        responder.setStatus(ResponderStatus.DISPATCHED);
        Responder saved = responderRepository.save(responder);
        availabilityCacheService.refresh();
        eventProducer.publishAssigned(request.assignmentId(), id, request.incidentId());
        return responderMapper.toResponse(saved);
    }

    public ResponderResponse release(Long id) {
        Responder responder = findOrThrow(id);
        responder.setStatus(ResponderStatus.AVAILABLE);
        Responder saved = responderRepository.save(responder);
        availabilityCacheService.refresh();
        return responderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return responderRepository.countByStatus(ResponderStatus.AVAILABLE) + responderRepository.countByStatus(ResponderStatus.DISPATCHED);
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return responderRepository.countByStatus(ResponderStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return responderRepository.count();
    }

    private Responder findOrThrow(Long id) {
        return responderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Responder", id));
    }
}

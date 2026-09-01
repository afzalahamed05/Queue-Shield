package com.queueshield.responder;

import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.responder.dto.ResponderMapper;
import com.queueshield.responder.dto.ResponderRequest;
import com.queueshield.responder.dto.ResponderResponse;
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

    public ResponderService(ResponderRepository responderRepository, ResponderMapper responderMapper) {
        this.responderRepository = responderRepository;
        this.responderMapper = responderMapper;
    }

    public ResponderResponse create(@Valid ResponderRequest request) {
        Responder responder = new Responder();
        responderMapper.applyToEntity(request, responder);
        return responderMapper.toResponse(responderRepository.save(responder));
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
        return responderMapper.toResponse(responderRepository.save(responder));
    }

    public void delete(Long id) {
        if (!responderRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Responder", id);
        }
        responderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return responderRepository.countByStatus(ResponderStatus.AVAILABLE)
                + responderRepository.countByStatus(ResponderStatus.DISPATCHED);
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

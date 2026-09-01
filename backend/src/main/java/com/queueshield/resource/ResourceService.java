package com.queueshield.resource;

import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.resource.dto.ResourceMapper;
import com.queueshield.resource.dto.ResourceRequest;
import com.queueshield.resource.dto.ResourceResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
    }

    public ResourceResponse create(@Valid ResourceRequest request) {
        validateQuantities(request);
        Resource resource = new Resource();
        resourceMapper.applyToEntity(request, resource);
        resource.recomputeStatus();
        return resourceMapper.toResponse(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Long id) {
        return resourceMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> list(ResourceType type, ResourceStatus status, Pageable pageable) {
        Page<Resource> page;
        if (type != null && status != null) {
            page = resourceRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            page = resourceRepository.findByType(type, pageable);
        } else if (status != null) {
            page = resourceRepository.findByStatus(status, pageable);
        } else {
            page = resourceRepository.findAll(pageable);
        }
        return page.map(resourceMapper::toResponse);
    }

    public ResourceResponse update(Long id, @Valid ResourceRequest request) {
        validateQuantities(request);
        Resource resource = findOrThrow(id);
        resourceMapper.applyToEntity(request, resource);
        resource.recomputeStatus();
        return resourceMapper.toResponse(resourceRepository.save(resource));
    }

    public void delete(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Resource", id);
        }
        resourceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return resourceRepository.sumQuantityAvailable();
    }

    private Resource findOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Resource", id));
    }

    private void validateQuantities(ResourceRequest request) {
        if (request.quantityAvailable() > request.quantityTotal()) {
            throw new IllegalArgumentException("quantityAvailable cannot exceed quantityTotal");
        }
    }
}

package com.queueshield.resourceservice.resource;

import com.queueshield.resourceservice.common.exception.ResourceNotFoundException;
import com.queueshield.resourceservice.resource.dto.ResourceMapper;
import com.queueshield.resourceservice.resource.dto.ResourceRequest;
import com.queueshield.resourceservice.resource.dto.ResourceResponse;
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
    private final ResourceAvailabilityCacheService availabilityCacheService;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper,
                            ResourceAvailabilityCacheService availabilityCacheService) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
        this.availabilityCacheService = availabilityCacheService;
    }

    public ResourceResponse create(@Valid ResourceRequest request) {
        validateQuantities(request);
        Resource resource = new Resource();
        resourceMapper.applyToEntity(request, resource);
        resource.recomputeStatus();
        Resource saved = resourceRepository.save(resource);
        availabilityCacheService.refresh();
        return resourceMapper.toResponse(saved);
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
        Resource saved = resourceRepository.save(resource);
        availabilityCacheService.refresh();
        return resourceMapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Resource", id);
        }
        resourceRepository.deleteById(id);
        availabilityCacheService.refresh();
    }

    /**
     * Symmetric compensating action for the reservation made via the ResourceRequested/Assigned
     * saga - but deliberately synchronous, not another async round trip. Releasing a unit doesn't
     * need arbitration or contention handling the way reserving one does (that's what justified
     * the async saga in the first place), so a plain REST call is simpler and just as correct.
     * Called by assignment-service when an assignment holding a reserved unit completes/cancels.
     */
    public ResourceResponse release(Long id) {
        Resource resource = findOrThrow(id);
        resource.releaseOneUnit();
        Resource saved = resourceRepository.save(resource);
        availabilityCacheService.refresh();
        return resourceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public long[] availabilityAndTotal() {
        return availabilityCacheService.getAvailableAndTotal();
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

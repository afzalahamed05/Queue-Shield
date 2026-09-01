package com.queueshield.shelterservice.shelter;

import com.queueshield.shelterservice.common.exception.ResourceNotFoundException;
import com.queueshield.shelterservice.event.ShelterEventProducer;
import com.queueshield.shelterservice.shelter.dto.ShelterMapper;
import com.queueshield.shelterservice.shelter.dto.ShelterRequest;
import com.queueshield.shelterservice.shelter.dto.ShelterResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShelterService {

    private final ShelterRepository shelterRepository;
    private final ShelterMapper shelterMapper;
    private final ShelterCapacityCacheService capacityCacheService;
    private final ShelterEventProducer eventProducer;

    public ShelterService(ShelterRepository shelterRepository, ShelterMapper shelterMapper,
                           ShelterCapacityCacheService capacityCacheService, ShelterEventProducer eventProducer) {
        this.shelterRepository = shelterRepository;
        this.shelterMapper = shelterMapper;
        this.capacityCacheService = capacityCacheService;
        this.eventProducer = eventProducer;
    }

    public ShelterResponse create(@Valid ShelterRequest request) {
        validateCapacity(request);
        Shelter shelter = new Shelter();
        shelterMapper.applyToEntity(request, shelter);
        shelter.recomputeStatus();
        Shelter saved = shelterRepository.save(shelter);
        capacityCacheService.refresh();
        eventProducer.publishCapacityChanged(saved);
        return shelterMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShelterResponse getById(Long id) {
        return shelterMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ShelterResponse> list(ShelterStatus status, Pageable pageable) {
        Page<Shelter> page = status != null ? shelterRepository.findByStatus(status, pageable) : shelterRepository.findAll(pageable);
        return page.map(shelterMapper::toResponse);
    }

    public ShelterResponse update(Long id, @Valid ShelterRequest request) {
        validateCapacity(request);
        Shelter shelter = findOrThrow(id);
        shelterMapper.applyToEntity(request, shelter);
        shelter.recomputeStatus();
        Shelter saved = shelterRepository.save(shelter);
        capacityCacheService.refresh();
        eventProducer.publishCapacityChanged(saved);
        return shelterMapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!shelterRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Shelter", id);
        }
        shelterRepository.deleteById(id);
        capacityCacheService.refresh();
    }

    @Transactional(readOnly = true)
    public long[] capacitySummary() {
        return capacityCacheService.getCapacity();
    }

    private Shelter findOrThrow(Long id) {
        return shelterRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Shelter", id));
    }

    private void validateCapacity(ShelterRequest request) {
        if (request.capacityOccupied() > request.capacityTotal()) {
            throw new IllegalArgumentException("capacityOccupied cannot exceed capacityTotal");
        }
    }
}

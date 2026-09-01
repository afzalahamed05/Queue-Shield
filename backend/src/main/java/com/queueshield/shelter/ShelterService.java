package com.queueshield.shelter;

import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.shelter.dto.ShelterMapper;
import com.queueshield.shelter.dto.ShelterRequest;
import com.queueshield.shelter.dto.ShelterResponse;
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

    public ShelterService(ShelterRepository shelterRepository, ShelterMapper shelterMapper) {
        this.shelterRepository = shelterRepository;
        this.shelterMapper = shelterMapper;
    }

    public ShelterResponse create(@Valid ShelterRequest request) {
        validateCapacity(request);
        Shelter shelter = new Shelter();
        shelterMapper.applyToEntity(request, shelter);
        shelter.recomputeStatus();
        return shelterMapper.toResponse(shelterRepository.save(shelter));
    }

    @Transactional(readOnly = true)
    public ShelterResponse getById(Long id) {
        return shelterMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ShelterResponse> list(ShelterStatus status, Pageable pageable) {
        Page<Shelter> page = status != null
                ? shelterRepository.findByStatus(status, pageable)
                : shelterRepository.findAll(pageable);
        return page.map(shelterMapper::toResponse);
    }

    public ShelterResponse update(Long id, @Valid ShelterRequest request) {
        validateCapacity(request);
        Shelter shelter = findOrThrow(id);
        shelterMapper.applyToEntity(request, shelter);
        shelter.recomputeStatus();
        return shelterMapper.toResponse(shelterRepository.save(shelter));
    }

    public void delete(Long id) {
        if (!shelterRepository.existsById(id)) {
            throw ResourceNotFoundException.forEntity("Shelter", id);
        }
        shelterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long totalCapacity() {
        return shelterRepository.sumCapacityTotal();
    }

    @Transactional(readOnly = true)
    public long availableCapacity() {
        return Math.max(0, shelterRepository.sumCapacityTotal() - shelterRepository.sumCapacityOccupied());
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

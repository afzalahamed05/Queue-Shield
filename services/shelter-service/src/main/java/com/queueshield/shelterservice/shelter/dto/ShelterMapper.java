package com.queueshield.shelterservice.shelter.dto;

import com.queueshield.shelterservice.shelter.Shelter;
import org.springframework.stereotype.Component;

@Component
public class ShelterMapper {

    public ShelterResponse toResponse(Shelter shelter) {
        return new ShelterResponse(shelter.getId(), shelter.getName(), shelter.getAddress(),
                shelter.getCapacityTotal(), shelter.getCapacityOccupied(), shelter.getCapacityAvailable(),
                shelter.getContactPhone(), shelter.getStatus());
    }

    public void applyToEntity(ShelterRequest request, Shelter target) {
        target.setName(request.name());
        target.setAddress(request.address());
        target.setCapacityTotal(request.capacityTotal());
        target.setCapacityOccupied(request.capacityOccupied());
        target.setContactPhone(request.contactPhone());
    }
}

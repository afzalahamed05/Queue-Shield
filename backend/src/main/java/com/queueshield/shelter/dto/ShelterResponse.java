package com.queueshield.shelter.dto;

import com.queueshield.shelter.ShelterStatus;

public record ShelterResponse(
        Long id,
        String name,
        String address,
        int capacityTotal,
        int capacityOccupied,
        int capacityAvailable,
        String contactPhone,
        ShelterStatus status
) {
}

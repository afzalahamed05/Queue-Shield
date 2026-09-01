package com.queueshield.shelterservice.shelter.dto;

import com.queueshield.shelterservice.shelter.ShelterStatus;

public record ShelterResponse(Long id, String name, String address, int capacityTotal, int capacityOccupied,
                               int capacityAvailable, String contactPhone, ShelterStatus status) {
}

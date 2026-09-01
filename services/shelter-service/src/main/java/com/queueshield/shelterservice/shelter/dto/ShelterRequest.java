package com.queueshield.shelterservice.shelter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShelterRequest(
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @NotBlank(message = "address is required") @Size(max = 300) String address,
        @Min(0) int capacityTotal,
        @Min(0) int capacityOccupied,
        @NotBlank(message = "contactPhone is required") @Size(max = 30) String contactPhone
) {
}

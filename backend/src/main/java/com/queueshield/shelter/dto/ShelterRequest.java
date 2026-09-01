package com.queueshield.shelter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShelterRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @NotBlank(message = "address is required")
        @Size(max = 300, message = "address must be at most 300 characters")
        String address,

        @Min(value = 0, message = "capacityTotal cannot be negative")
        int capacityTotal,

        @Min(value = 0, message = "capacityOccupied cannot be negative")
        int capacityOccupied,

        @NotBlank(message = "contactPhone is required")
        @Size(max = 30, message = "contactPhone must be at most 30 characters")
        String contactPhone
) {
}

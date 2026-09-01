package com.queueshield.resource.dto;

import com.queueshield.resource.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @NotNull(message = "type is required")
        ResourceType type,

        @Min(value = 0, message = "quantityTotal cannot be negative")
        int quantityTotal,

        @Min(value = 0, message = "quantityAvailable cannot be negative")
        int quantityAvailable,

        @NotBlank(message = "location is required")
        @Size(max = 300, message = "location must be at most 300 characters")
        String location
) {
}

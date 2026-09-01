package com.queueshield.resourceservice.resource.dto;

import com.queueshield.resourceservice.resource.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceRequest(
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @NotNull(message = "type is required") ResourceType type,
        @Min(0) int quantityTotal,
        @Min(0) int quantityAvailable,
        @NotBlank(message = "location is required") @Size(max = 300) String location
) {
}

package com.queueshield.resource.dto;

import com.queueshield.resource.ResourceStatus;
import com.queueshield.resource.ResourceType;

public record ResourceResponse(
        Long id,
        String name,
        ResourceType type,
        int quantityTotal,
        int quantityAvailable,
        String location,
        ResourceStatus status
) {
}

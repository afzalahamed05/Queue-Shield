package com.queueshield.resourceservice.resource.dto;

import com.queueshield.resourceservice.resource.ResourceStatus;
import com.queueshield.resourceservice.resource.ResourceType;

public record ResourceResponse(
        Long id, String name, ResourceType type, int quantityTotal, int quantityAvailable,
        String location, ResourceStatus status
) {
}

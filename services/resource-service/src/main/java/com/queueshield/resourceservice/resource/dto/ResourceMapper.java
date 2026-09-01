package com.queueshield.resourceservice.resource.dto;

import com.queueshield.resourceservice.resource.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(resource.getId(), resource.getName(), resource.getType(),
                resource.getQuantityTotal(), resource.getQuantityAvailable(), resource.getLocation(), resource.getStatus());
    }

    public void applyToEntity(ResourceRequest request, Resource target) {
        target.setName(request.name());
        target.setType(request.type());
        target.setQuantityTotal(request.quantityTotal());
        target.setQuantityAvailable(request.quantityAvailable());
        target.setLocation(request.location());
    }
}

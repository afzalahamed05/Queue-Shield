package com.queueshield.responderservice.responder.dto;

import com.queueshield.responderservice.responder.Responder;
import com.queueshield.responderservice.responder.ResponderStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponderMapper {

    public ResponderResponse toResponse(Responder responder) {
        return new ResponderResponse(responder.getId(), responder.getName(), responder.getRole(),
                responder.getPhone(), responder.getStatus(), responder.getCurrentLocation());
    }

    public void applyToEntity(ResponderRequest request, Responder target) {
        target.setName(request.name());
        target.setRole(request.role());
        target.setPhone(request.phone());
        target.setCurrentLocation(request.currentLocation());
        target.setStatus(request.status() != null ? request.status() : ResponderStatus.AVAILABLE);
    }
}

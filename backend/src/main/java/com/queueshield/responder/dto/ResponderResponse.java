package com.queueshield.responder.dto;

import com.queueshield.responder.ResponderRole;
import com.queueshield.responder.ResponderStatus;

public record ResponderResponse(
        Long id,
        String name,
        ResponderRole role,
        String phone,
        ResponderStatus status,
        String currentLocation
) {
}

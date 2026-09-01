package com.queueshield.responderservice.responder.dto;

import com.queueshield.responderservice.responder.ResponderRole;
import com.queueshield.responderservice.responder.ResponderStatus;

public record ResponderResponse(Long id, String name, ResponderRole role, String phone, ResponderStatus status, String currentLocation) {
}

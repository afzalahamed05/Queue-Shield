package com.queueshield.responderservice.responder.dto;

import com.queueshield.responderservice.responder.ResponderRole;
import com.queueshield.responderservice.responder.ResponderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResponderRequest(
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @NotNull(message = "role is required") ResponderRole role,
        @NotBlank(message = "phone is required") @Size(max = 30) String phone,
        ResponderStatus status,
        @Size(max = 300) String currentLocation
) {
}

package com.queueshield.responder.dto;

import com.queueshield.responder.ResponderRole;
import com.queueshield.responder.ResponderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResponderRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @NotNull(message = "role is required")
        ResponderRole role,

        @NotBlank(message = "phone is required")
        @Size(max = 30, message = "phone must be at most 30 characters")
        String phone,

        ResponderStatus status,

        @Size(max = 300, message = "currentLocation must be at most 300 characters")
        String currentLocation
) {
}

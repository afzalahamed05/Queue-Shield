package com.queueshield.notificationservice.notification.dto;

import com.queueshield.notificationservice.notification.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id, NotificationType type, String message, Long relatedEntityId, boolean read, Instant createdAt
) {
}

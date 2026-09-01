package com.queueshield.notificationservice.event;

import java.time.Instant;

public record ShelterCapacityChangedEvent(String eventId, Long shelterId, int capacityTotal, int capacityOccupied,
                                           int capacityAvailable, String status, Instant occurredAt) {
}

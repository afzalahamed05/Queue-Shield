package com.queueshield.notificationservice.event;

import com.queueshield.notificationservice.notification.Notification;
import com.queueshield.notificationservice.notification.NotificationRepository;
import com.queueshield.notificationservice.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Pure fan-out: reacts to four different event types from four different services and turns each
 * into a row in this service's own notification log. No idempotency guard here, deliberately -
 * unlike resource-service's reservation (where processing twice double-decrements) or
 * incident-service's priority cache (idempotent by construction), a duplicate notification from
 * an at-least-once redelivery is a harmless, acceptable cost. For an alert feed, occasionally
 * over-notifying is a much safer failure mode than under-notifying.
 */
@Component
@Slf4j
public class NotificationEventConsumer {

    private static final int SHELTER_LOW_CAPACITY_THRESHOLD = 10;

    private final NotificationRepository notificationRepository;

    public NotificationEventConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = Topics.INCIDENT_PRIORITIZED, groupId = "notification-service")
    public void onIncidentPrioritized(IncidentPrioritizedEvent event) {
        if (!"CRITICAL".equals(event.tier())) {
            return;
        }
        save(NotificationType.INCIDENT_CRITICAL,
                "Incident " + event.incidentId() + " reached CRITICAL priority (score " + event.score() + ")",
                event.incidentId());
    }

    @KafkaListener(topics = Topics.ASSIGNMENT_COMPLETED, groupId = "notification-service")
    public void onAssignmentCompleted(AssignmentCompletedEvent event) {
        save(NotificationType.ASSIGNMENT_COMPLETED,
                "Assignment " + event.assignmentId() + " for incident " + event.incidentId() + " completed",
                event.assignmentId());
    }

    @KafkaListener(topics = Topics.RESOURCE_REQUEST_REJECTED, groupId = "notification-service")
    public void onResourceRequestRejected(ResourceRequestRejectedEvent event) {
        save(NotificationType.RESOURCE_REQUEST_REJECTED,
                "Resource request for assignment " + event.assignmentId() + " was rejected: " + event.reason(),
                event.assignmentId());
    }

    @KafkaListener(topics = Topics.SHELTER_CAPACITY_CHANGED, groupId = "notification-service")
    public void onShelterCapacityChanged(ShelterCapacityChangedEvent event) {
        if (event.capacityAvailable() > SHELTER_LOW_CAPACITY_THRESHOLD) {
            return;
        }
        save(NotificationType.SHELTER_LOW_CAPACITY,
                "Shelter " + event.shelterId() + " is low on capacity (" + event.capacityAvailable() + " beds available)",
                event.shelterId());
    }

    private void save(NotificationType type, String message, Long relatedEntityId) {
        Notification notification = Notification.builder()
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
        log.info("Created notification: {}", message);
    }
}

package com.queueshield.resourceservice.event;

import com.queueshield.resourceservice.reservation.ResourceReservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class ResourceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ResourceEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAssigned(ResourceReservation reservation) {
        ResourceAssignedEvent event = new ResourceAssignedEvent(
                UUID.randomUUID().toString(), reservation.getAssignmentId(), reservation.getResourceId(),
                reservation.getIncidentId(), Instant.now());
        send(Topics.RESOURCE_ASSIGNED, String.valueOf(reservation.getAssignmentId()), event);
    }

    public void publishRejected(ResourceReservation reservation) {
        ResourceRequestRejectedEvent event = new ResourceRequestRejectedEvent(
                UUID.randomUUID().toString(), reservation.getAssignmentId(), reservation.getResourceId(),
                reservation.getIncidentId(), reservation.getRejectionReason(), Instant.now());
        send(Topics.RESOURCE_REQUEST_REJECTED, String.valueOf(reservation.getAssignmentId()), event);
    }

    private void send(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to {} for key {}", topic, key, ex);
            } else {
                log.debug("Published to {} for key {}", topic, key);
            }
        });
    }
}

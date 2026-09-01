package com.queueshield.resourceservice.event;

import com.queueshield.resourceservice.reservation.ResourceReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResourceRequestedConsumer {

    private final ResourceReservationService reservationService;

    public ResourceRequestedConsumer(ResourceReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @KafkaListener(topics = Topics.RESOURCE_REQUESTED, groupId = "resource-service")
    public void onResourceRequested(ResourceRequestedEvent event) {
        log.info("Received resource request: assignment={} resource={} incident={}",
                event.assignmentId(), event.resourceId(), event.incidentId());
        reservationService.handleRequest(event);
    }
}

package com.queueshield.priorityservice.event;

import com.queueshield.priorityservice.priority.PriorityCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * One handler for both topics: whether an incident was just created or just updated, the
 * reaction is identical - recompute from the state given. Recomputation is a pure function of
 * that state, so processing the same event twice (at-least-once redelivery) or receiving both a
 * create and a follow-up update in quick succession is always safe.
 */
@Component
@Slf4j
public class IncidentEventConsumer {

    private final PriorityCalculationService priorityCalculationService;

    public IncidentEventConsumer(PriorityCalculationService priorityCalculationService) {
        this.priorityCalculationService = priorityCalculationService;
    }

    @KafkaListener(topics = {Topics.INCIDENT_CREATED, Topics.INCIDENT_UPDATED}, groupId = "priority-service")
    public void onIncidentEvent(IncidentEvent event) {
        log.info("Received incident event for incident {} (severity={}, status={})",
                event.incidentId(), event.severity(), event.status());
        priorityCalculationService.recompute(event);
    }
}

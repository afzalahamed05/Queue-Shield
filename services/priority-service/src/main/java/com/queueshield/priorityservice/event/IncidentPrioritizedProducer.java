package com.queueshield.priorityservice.event;

import com.queueshield.priorityservice.priority.IncidentPriority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class IncidentPrioritizedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IncidentPrioritizedProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(IncidentPriority priority) {
        IncidentPrioritizedEvent event = new IncidentPrioritizedEvent(
                UUID.randomUUID().toString(),
                priority.getIncidentId(),
                priority.getScore(),
                priority.getTier().name(),
                priority.getComputedAt(),
                Instant.now()
        );
        String key = String.valueOf(priority.getIncidentId());
        kafkaTemplate.send(Topics.INCIDENT_PRIORITIZED, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish IncidentPrioritized for incident {}", priority.getIncidentId(), ex);
            } else {
                log.debug("Published IncidentPrioritized for incident {}: score={} tier={}",
                        priority.getIncidentId(), priority.getScore(), priority.getTier());
            }
        });
    }
}

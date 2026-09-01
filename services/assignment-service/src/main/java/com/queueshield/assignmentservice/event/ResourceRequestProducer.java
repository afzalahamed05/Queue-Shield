package com.queueshield.assignmentservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class ResourceRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ResourceRequestProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRequested(Long assignmentId, Long resourceId, Long incidentId) {
        ResourceRequestedEvent event = new ResourceRequestedEvent(
                UUID.randomUUID().toString(), assignmentId, resourceId, incidentId, Instant.now());
        kafkaTemplate.send(Topics.RESOURCE_REQUESTED, String.valueOf(assignmentId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ResourceRequested for assignment {}", assignmentId, ex);
                    }
                });
    }
}

package com.queueshield.responderservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class ResponderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ResponderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAssigned(Long assignmentId, Long responderId, Long incidentId) {
        ResponderAssignedEvent event = new ResponderAssignedEvent(
                UUID.randomUUID().toString(), assignmentId, responderId, incidentId, Instant.now());
        kafkaTemplate.send(Topics.RESPONDER_ASSIGNED, String.valueOf(assignmentId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ResponderAssigned for assignment {}", assignmentId, ex);
                    }
                });
    }
}

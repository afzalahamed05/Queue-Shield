package com.queueshield.assignmentservice.event;

import com.queueshield.assignmentservice.assignment.Assignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class AssignmentCompletedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AssignmentCompletedProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Assignment assignment) {
        AssignmentCompletedEvent event = new AssignmentCompletedEvent(
                UUID.randomUUID().toString(), assignment.getId(), assignment.getIncidentId(),
                assignment.getResponderId(), assignment.getResourceId(), assignment.getShelterId(), Instant.now());
        kafkaTemplate.send(Topics.ASSIGNMENT_COMPLETED, String.valueOf(assignment.getId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish AssignmentCompleted for assignment {}", assignment.getId(), ex);
                    }
                });
    }
}

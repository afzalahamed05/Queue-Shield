package com.queueshield.notificationservice.event;

import com.queueshield.notificationservice.notification.NotificationRepository;
import com.queueshield.notificationservice.notification.NotificationType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationEventConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void criticalIncidentPrioritizedCreatesNotification() {
        long incidentId = uniqueId();
        Instant now = Instant.now();
        kafkaTemplate.send(Topics.INCIDENT_PRIORITIZED, String.valueOf(incidentId),
                new IncidentPrioritizedEvent("evt-1", incidentId, 92.5, "CRITICAL", now, now));

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent())
                        .anyMatch(n -> n.getType() == NotificationType.INCIDENT_CRITICAL && incidentId == n.getRelatedEntityId()));
    }

    @Test
    void nonCriticalIncidentPrioritizedDoesNotCreateNotification() {
        long incidentId = uniqueId();
        Instant now = Instant.now();
        kafkaTemplate.send(Topics.INCIDENT_PRIORITIZED, String.valueOf(incidentId),
                new IncidentPrioritizedEvent("evt-2", incidentId, 40.0, "MEDIUM", now, now));

        // Publish a second, definitely-processed event afterward as a synchronization point,
        // then assert the MEDIUM one never produced a notification.
        long marker = uniqueId();
        kafkaTemplate.send(Topics.ASSIGNMENT_COMPLETED, String.valueOf(marker),
                new AssignmentCompletedEvent("evt-3", marker, 1L, null, null, null, now));

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent())
                        .anyMatch(n -> n.getType() == NotificationType.ASSIGNMENT_COMPLETED && marker == n.getRelatedEntityId()));

        assertThat(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 200)).getContent())
                .noneMatch(n -> n.getType() == NotificationType.INCIDENT_CRITICAL && incidentId == n.getRelatedEntityId());
    }

    @Test
    void lowShelterCapacityCreatesNotificationButHighCapacityDoesNot() {
        long lowShelterId = uniqueId();
        long highShelterId = lowShelterId + 1;
        Instant now = Instant.now();

        kafkaTemplate.send(Topics.SHELTER_CAPACITY_CHANGED, String.valueOf(lowShelterId),
                new ShelterCapacityChangedEvent("evt-4", lowShelterId, 100, 95, 5, "OPEN", now));
        kafkaTemplate.send(Topics.SHELTER_CAPACITY_CHANGED, String.valueOf(highShelterId),
                new ShelterCapacityChangedEvent("evt-5", highShelterId, 100, 10, 90, "OPEN", now));

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent())
                        .anyMatch(n -> n.getType() == NotificationType.SHELTER_LOW_CAPACITY && lowShelterId == n.getRelatedEntityId()));

        assertThat(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 200)).getContent())
                .noneMatch(n -> n.getType() == NotificationType.SHELTER_LOW_CAPACITY && highShelterId == n.getRelatedEntityId());
    }

    private long uniqueId() {
        return System.currentTimeMillis() + Thread.currentThread().threadId();
    }
}

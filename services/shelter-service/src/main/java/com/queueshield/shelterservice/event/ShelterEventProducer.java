package com.queueshield.shelterservice.event;

import com.queueshield.shelterservice.shelter.Shelter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class ShelterEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShelterEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCapacityChanged(Shelter shelter) {
        ShelterCapacityChangedEvent event = new ShelterCapacityChangedEvent(
                UUID.randomUUID().toString(), shelter.getId(), shelter.getCapacityTotal(),
                shelter.getCapacityOccupied(), shelter.getCapacityAvailable(), shelter.getStatus().name(), Instant.now());
        kafkaTemplate.send(Topics.SHELTER_CAPACITY_CHANGED, String.valueOf(shelter.getId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ShelterCapacityChanged for shelter {}", shelter.getId(), ex);
                    }
                });
    }
}

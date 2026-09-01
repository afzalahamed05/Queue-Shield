package com.queueshield.resourceservice.reservation;

import com.queueshield.resourceservice.event.ResourceEventProducer;
import com.queueshield.resourceservice.event.ResourceRequestedEvent;
import com.queueshield.resourceservice.resource.Resource;
import com.queueshield.resourceservice.resource.ResourceAvailabilityCacheService;
import com.queueshield.resourceservice.resource.ResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class ResourceReservationService {

    private final ResourceReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAvailabilityCacheService availabilityCacheService;
    private final ResourceEventProducer producer;

    public ResourceReservationService(ResourceReservationRepository reservationRepository,
                                       ResourceRepository resourceRepository,
                                       ResourceAvailabilityCacheService availabilityCacheService,
                                       ResourceEventProducer producer) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.availabilityCacheService = availabilityCacheService;
        this.producer = producer;
    }

    @Transactional
    public void handleRequest(ResourceRequestedEvent event) {
        Optional<ResourceReservation> existing = reservationRepository.findByAssignmentId(event.assignmentId());
        if (existing.isPresent()) {
            log.info("Assignment {} already processed (status={}) - republishing outcome, not re-reserving",
                    event.assignmentId(), existing.get().getStatus());
            republish(existing.get());
            return;
        }

        try {
            ResourceReservation reservation = reserve(event);
            reservationRepository.save(reservation);
            republish(reservation);
        } catch (DataIntegrityViolationException raceOnUniqueAssignmentId) {
            // Another thread/instance processed this assignmentId between our check and our insert.
            log.info("Lost a race on assignment {} - treating as already handled", event.assignmentId());
            reservationRepository.findByAssignmentId(event.assignmentId()).ifPresent(this::republish);
        }
    }

    private ResourceReservation reserve(ResourceRequestedEvent event) {
        Optional<Resource> resourceOpt = resourceRepository.findById(event.resourceId());
        if (resourceOpt.isEmpty()) {
            return rejection(event, "Resource " + event.resourceId() + " does not exist");
        }

        Resource resource = resourceOpt.get();
        if (!resource.tryReserveOneUnit()) {
            return rejection(event, "No available units for resource " + event.resourceId());
        }

        resourceRepository.save(resource);
        availabilityCacheService.refresh();

        return ResourceReservation.builder()
                .assignmentId(event.assignmentId())
                .resourceId(event.resourceId())
                .incidentId(event.incidentId())
                .status(ReservationStatus.RESERVED)
                .processedAt(Instant.now())
                .build();
    }

    private ResourceReservation rejection(ResourceRequestedEvent event, String reason) {
        log.info("Rejecting resource request for assignment {}: {}", event.assignmentId(), reason);
        return ResourceReservation.builder()
                .assignmentId(event.assignmentId())
                .resourceId(event.resourceId())
                .incidentId(event.incidentId())
                .status(ReservationStatus.REJECTED)
                .rejectionReason(reason)
                .processedAt(Instant.now())
                .build();
    }

    private void republish(ResourceReservation reservation) {
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            producer.publishAssigned(reservation);
        } else {
            producer.publishRejected(reservation);
        }
    }
}

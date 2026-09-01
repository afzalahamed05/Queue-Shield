package com.queueshield.assignment;

import com.queueshield.assignment.dto.AssignmentRequest;
import com.queueshield.assignment.dto.AssignmentResponse;
import com.queueshield.common.exception.BusinessRuleViolationException;
import com.queueshield.common.exception.ResourceNotFoundException;
import com.queueshield.incident.Incident;
import com.queueshield.incident.IncidentRepository;
import com.queueshield.incident.IncidentStatus;
import com.queueshield.priority.PriorityTier;
import com.queueshield.priority.Severity;
import com.queueshield.resource.Resource;
import com.queueshield.resource.ResourceRepository;
import com.queueshield.resource.ResourceStatus;
import com.queueshield.resource.ResourceType;
import com.queueshield.responder.Responder;
import com.queueshield.responder.ResponderRepository;
import com.queueshield.responder.ResponderRole;
import com.queueshield.responder.ResponderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AssignmentServiceIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private ResponderRepository responderRepository;
    @Autowired
    private ResourceRepository resourceRepository;

    private Incident incident;
    private Responder responder;
    private Resource resource;

    @BeforeEach
    void setUp() {
        incident = incidentRepository.save(Incident.builder()
                .title("Test incident")
                .description("desc")
                .location("Somewhere")
                .severity(Severity.HIGH)
                .status(IncidentStatus.REPORTED)
                .peopleAffected(10)
                .vulnerablePopulationCount(2)
                .reportedAt(Instant.now())
                .updatedAt(Instant.now())
                .priorityScore(50.0)
                .priorityTier(PriorityTier.HIGH)
                .build());

        responder = responderRepository.save(Responder.builder()
                .name("Unit 7")
                .role(ResponderRole.FIRE)
                .phone("555-0100")
                .status(ResponderStatus.AVAILABLE)
                .currentLocation("Station 3")
                .build());

        resource = resourceRepository.save(Resource.builder()
                .name("Ambulance")
                .type(ResourceType.VEHICLE)
                .quantityTotal(5)
                .quantityAvailable(5)
                .location("Depot")
                .status(ResourceStatus.AVAILABLE)
                .build());
    }

    @Test
    void assignmentWithNoTargetIsRejected() {
        AssignmentRequest request = new AssignmentRequest(incident.getId(), null, null, null, null, "notes");

        assertThatThrownBy(() -> assignmentService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void assignmentWithUnknownIncidentIsRejected() {
        AssignmentRequest request = new AssignmentRequest(999_999L, responder.getId(), null, null, null, "notes");

        assertThatThrownBy(() -> assignmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creatingAssignmentDispatchesResponderAndCommitsResourceUnit() {
        AssignmentRequest request = new AssignmentRequest(
                incident.getId(), responder.getId(), resource.getId(), null, null, "Send unit 7");

        AssignmentResponse response = assignmentService.create(request);

        assertThat(response.status()).isEqualTo(AssignmentStatus.PENDING);

        Responder reloadedResponder = responderRepository.findById(responder.getId()).orElseThrow();
        assertThat(reloadedResponder.getStatus()).isEqualTo(ResponderStatus.DISPATCHED);

        Resource reloadedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertThat(reloadedResource.getQuantityAvailable()).isEqualTo(4);
    }

    @Test
    void completingAssignmentReleasesResponderAndResource() {
        AssignmentRequest request = new AssignmentRequest(
                incident.getId(), responder.getId(), resource.getId(), null, null, null);
        AssignmentResponse created = assignmentService.create(request);

        assignmentService.updateStatus(created.id(), AssignmentStatus.COMPLETED);

        Responder reloadedResponder = responderRepository.findById(responder.getId()).orElseThrow();
        assertThat(reloadedResponder.getStatus()).isEqualTo(ResponderStatus.AVAILABLE);

        Resource reloadedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertThat(reloadedResource.getQuantityAvailable()).isEqualTo(5);
    }

    @Test
    void dispatchingAnOffDutyResponderIsRejected() {
        responder.setStatus(ResponderStatus.OFF_DUTY);
        responderRepository.save(responder);

        AssignmentRequest request = new AssignmentRequest(
                incident.getId(), responder.getId(), null, null, null, null);

        assertThatThrownBy(() -> assignmentService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void committingAResourceWithNoAvailableUnitsIsRejected() {
        resource.setQuantityAvailable(0);
        resourceRepository.save(resource);

        AssignmentRequest request = new AssignmentRequest(
                incident.getId(), null, resource.getId(), null, null, null);

        assertThatThrownBy(() -> assignmentService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void deletingActiveAssignmentReleasesResponder() {
        AssignmentRequest request = new AssignmentRequest(
                incident.getId(), responder.getId(), null, null, null, null);
        AssignmentResponse created = assignmentService.create(request);

        assignmentService.delete(created.id());

        Responder reloadedResponder = responderRepository.findById(responder.getId()).orElseThrow();
        assertThat(reloadedResponder.getStatus()).isEqualTo(ResponderStatus.AVAILABLE);
    }
}

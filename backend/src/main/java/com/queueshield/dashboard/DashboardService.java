package com.queueshield.dashboard;

import com.queueshield.incident.IncidentService;
import com.queueshield.resource.ResourceService;
import com.queueshield.responder.ResponderService;
import com.queueshield.shelter.ShelterService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final IncidentService incidentService;
    private final ResponderService responderService;
    private final ResourceService resourceService;
    private final ShelterService shelterService;

    public DashboardService(IncidentService incidentService, ResponderService responderService,
                             ResourceService resourceService, ShelterService shelterService) {
        this.incidentService = incidentService;
        this.responderService = responderService;
        this.resourceService = resourceService;
        this.shelterService = shelterService;
    }

    public DashboardResponse getSummary() {
        var recent = incidentService.list(null, null, null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "reportedAt")));

        return new DashboardResponse(
                incidentService.countAll(),
                incidentService.countActive(),
                incidentService.countCritical(),
                responderService.countActive(),
                responderService.countAll(),
                resourceService.countAvailable(),
                shelterService.totalCapacity(),
                shelterService.availableCapacity(),
                recent.getContent()
        );
    }
}

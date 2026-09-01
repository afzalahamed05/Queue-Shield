package com.queueshield.dashboard;

import com.queueshield.incident.dto.IncidentResponse;

import java.util.List;

public record DashboardResponse(
        long totalIncidents,
        long activeIncidents,
        long criticalIncidents,
        long activeResponders,
        long totalResponders,
        long availableResourceUnits,
        long shelterCapacityTotal,
        long shelterCapacityAvailable,
        List<IncidentResponse> recentIncidents
) {
}

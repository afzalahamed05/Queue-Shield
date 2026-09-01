package com.queueshield.incidentservice.incident;

public record IncidentSummaryResponse(long total, long active, long critical) {
}

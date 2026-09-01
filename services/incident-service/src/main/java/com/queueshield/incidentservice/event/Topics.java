package com.queueshield.incidentservice.event;

/** Kafka topic names this service publishes to or consumes from. */
public final class Topics {

    public static final String INCIDENT_CREATED = "incident.created";
    public static final String INCIDENT_UPDATED = "incident.updated";
    public static final String INCIDENT_PRIORITIZED = "incident.prioritized";

    private Topics() {
    }
}

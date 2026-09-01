export type Severity = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';

export type IncidentStatus = 'REPORTED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

export type PriorityTier = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/**
 * priorityScore/priorityTier are null until priority-service processes this incident's
 * IncidentCreated event and publishes IncidentPrioritized back - normally a few hundred
 * milliseconds, but incident-service never blocks on it. The full per-factor breakdown isn't
 * part of this response at all anymore - see core/services/priority.service.ts, which reads it
 * from priority-service directly (a separate call, a separate service, a separate database).
 */
export interface Incident {
  id: number;
  title: string;
  description: string | null;
  location: string;
  severity: Severity;
  status: IncidentStatus;
  peopleAffected: number;
  vulnerablePopulationCount: number;
  reportedAt: string;
  updatedAt: string;
  priorityScore: number | null;
  priorityTier: PriorityTier | null;
  priorityComputedAt: string | null;
}

export interface IncidentRequest {
  title: string;
  description: string | null;
  location: string;
  severity: Severity;
  status: IncidentStatus | null;
  peopleAffected: number;
  vulnerablePopulationCount: number;
}

export const SEVERITY_OPTIONS: Severity[] = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL'];
export const INCIDENT_STATUS_OPTIONS: IncidentStatus[] = [
  'REPORTED',
  'ACKNOWLEDGED',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
];

import { PriorityTier } from './incident.model';

/** Owned by priority-service, not incident-service - see core/services/priority.service.ts. */
export interface PriorityBreakdown {
  incidentId: number;
  score: number;
  tier: PriorityTier;
  severityComponent: number;
  peopleAffectedComponent: number;
  vulnerabilityComponent: number;
  urgencyComponent: number;
  resourceScarcityComponent: number;
  computedAt: string;
}

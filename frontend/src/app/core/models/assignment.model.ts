export type AssignmentStatus = 'PENDING' | 'EN_ROUTE' | 'ON_SITE' | 'COMPLETED' | 'CANCELLED';

export type ResourceRequestStatus = 'NOT_REQUESTED' | 'PENDING' | 'ASSIGNED' | 'REJECTED';

/**
 * Only ids, not names - assignment-service can no longer join to Incident/Responder/Resource for
 * a display-friendly title (those live in other services' databases now). The UI resolves ids to
 * names itself by looking the entity up via its own service - see incident-detail.component.ts
 * for an example.
 */
export interface Assignment {
  id: number;
  incidentId: number;
  responderId: number | null;
  resourceId: number | null;
  shelterId: number | null;
  status: AssignmentStatus;
  resourceRequestStatus: ResourceRequestStatus;
  notes: string | null;
  assignedAt: string;
}

export interface AssignmentRequest {
  incidentId: number;
  responderId: number | null;
  resourceId: number | null;
  shelterId: number | null;
  notes: string | null;
}

export const ASSIGNMENT_STATUS_OPTIONS: AssignmentStatus[] = [
  'PENDING',
  'EN_ROUTE',
  'ON_SITE',
  'COMPLETED',
  'CANCELLED',
];

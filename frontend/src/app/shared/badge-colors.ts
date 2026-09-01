/** Centralized status/severity -> badge color mapping, kept separate from markup so every list
 * and detail view renders the same color for the same status. */

const SEVERITY_COLORS: Record<string, string> = {
  LOW: 'badge-neutral',
  MODERATE: 'badge-amber',
  HIGH: 'badge-orange',
  CRITICAL: 'badge-red',
};

const PRIORITY_TIER_COLORS: Record<string, string> = {
  LOW: 'badge-neutral',
  MEDIUM: 'badge-amber',
  HIGH: 'badge-orange',
  CRITICAL: 'badge-red',
};

const INCIDENT_STATUS_COLORS: Record<string, string> = {
  REPORTED: 'badge-blue',
  ACKNOWLEDGED: 'badge-amber',
  IN_PROGRESS: 'badge-orange',
  RESOLVED: 'badge-green',
  CLOSED: 'badge-neutral',
};

const RESPONDER_STATUS_COLORS: Record<string, string> = {
  AVAILABLE: 'badge-green',
  DISPATCHED: 'badge-blue',
  OFF_DUTY: 'badge-neutral',
  UNAVAILABLE: 'badge-red',
};

const RESOURCE_STATUS_COLORS: Record<string, string> = {
  AVAILABLE: 'badge-green',
  LOW: 'badge-amber',
  DEPLETED: 'badge-red',
  OUT_OF_SERVICE: 'badge-neutral',
};

const SHELTER_STATUS_COLORS: Record<string, string> = {
  OPEN: 'badge-green',
  FULL: 'badge-amber',
  CLOSED: 'badge-neutral',
};

const ASSIGNMENT_STATUS_COLORS: Record<string, string> = {
  PENDING: 'badge-blue',
  EN_ROUTE: 'badge-amber',
  ON_SITE: 'badge-orange',
  COMPLETED: 'badge-green',
  CANCELLED: 'badge-neutral',
};

const NOTIFICATION_TYPE_COLORS: Record<string, string> = {
  INCIDENT_CRITICAL: 'badge-red',
  ASSIGNMENT_COMPLETED: 'badge-green',
  RESOURCE_REQUEST_REJECTED: 'badge-orange',
  SHELTER_LOW_CAPACITY: 'badge-amber',
};

function lookup(map: Record<string, string>, key: string): string {
  return map[key] ?? 'badge-neutral';
}

export const severityColor = (value: string) => lookup(SEVERITY_COLORS, value);
export const priorityTierColor = (value: string) => lookup(PRIORITY_TIER_COLORS, value);
export const incidentStatusColor = (value: string) => lookup(INCIDENT_STATUS_COLORS, value);
export const responderStatusColor = (value: string) => lookup(RESPONDER_STATUS_COLORS, value);
export const resourceStatusColor = (value: string) => lookup(RESOURCE_STATUS_COLORS, value);
export const shelterStatusColor = (value: string) => lookup(SHELTER_STATUS_COLORS, value);
export const assignmentStatusColor = (value: string) => lookup(ASSIGNMENT_STATUS_COLORS, value);
export const notificationTypeColor = (value: string) => lookup(NOTIFICATION_TYPE_COLORS, value);

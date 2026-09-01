export type NotificationType =
  | 'INCIDENT_CRITICAL'
  | 'ASSIGNMENT_COMPLETED'
  | 'RESOURCE_REQUEST_REJECTED'
  | 'SHELTER_LOW_CAPACITY';

export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
  relatedEntityId: number | null;
  read: boolean;
  createdAt: string;
}

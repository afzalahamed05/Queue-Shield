export type ResponderRole = 'FIRE' | 'MEDICAL' | 'POLICE' | 'SEARCH_AND_RESCUE' | 'LOGISTICS' | 'VOLUNTEER';

export type ResponderStatus = 'AVAILABLE' | 'DISPATCHED' | 'OFF_DUTY' | 'UNAVAILABLE';

export interface Responder {
  id: number;
  name: string;
  role: ResponderRole;
  phone: string;
  status: ResponderStatus;
  currentLocation: string | null;
}

export interface ResponderRequest {
  name: string;
  role: ResponderRole;
  phone: string;
  status: ResponderStatus | null;
  currentLocation: string | null;
}

export const RESPONDER_ROLE_OPTIONS: ResponderRole[] = [
  'FIRE',
  'MEDICAL',
  'POLICE',
  'SEARCH_AND_RESCUE',
  'LOGISTICS',
  'VOLUNTEER',
];

export const RESPONDER_STATUS_OPTIONS: ResponderStatus[] = ['AVAILABLE', 'DISPATCHED', 'OFF_DUTY', 'UNAVAILABLE'];

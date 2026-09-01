export type ResourceType =
  | 'MEDICAL_SUPPLIES'
  | 'RESCUE_BOAT'
  | 'VEHICLE'
  | 'FOOD'
  | 'WATER'
  | 'SHELTER_KIT'
  | 'COMMUNICATION_EQUIPMENT'
  | 'POWER_GENERATOR'
  | 'OTHER';

export type ResourceStatus = 'AVAILABLE' | 'LOW' | 'DEPLETED' | 'OUT_OF_SERVICE';

export interface EmergencyResource {
  id: number;
  name: string;
  type: ResourceType;
  quantityTotal: number;
  quantityAvailable: number;
  location: string;
  status: ResourceStatus;
}

export interface ResourceRequest {
  name: string;
  type: ResourceType;
  quantityTotal: number;
  quantityAvailable: number;
  location: string;
}

export const RESOURCE_TYPE_OPTIONS: ResourceType[] = [
  'MEDICAL_SUPPLIES',
  'RESCUE_BOAT',
  'VEHICLE',
  'FOOD',
  'WATER',
  'SHELTER_KIT',
  'COMMUNICATION_EQUIPMENT',
  'POWER_GENERATOR',
  'OTHER',
];

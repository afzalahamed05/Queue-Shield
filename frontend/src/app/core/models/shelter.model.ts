export type ShelterStatus = 'OPEN' | 'FULL' | 'CLOSED';

export interface Shelter {
  id: number;
  name: string;
  address: string;
  capacityTotal: number;
  capacityOccupied: number;
  capacityAvailable: number;
  contactPhone: string;
  status: ShelterStatus;
}

export interface ShelterRequest {
  name: string;
  address: string;
  capacityTotal: number;
  capacityOccupied: number;
  contactPhone: string;
}

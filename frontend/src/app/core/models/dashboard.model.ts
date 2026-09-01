import { Incident } from './incident.model';

export interface DashboardSummary {
  totalIncidents: number;
  activeIncidents: number;
  criticalIncidents: number;
  activeResponders: number;
  totalResponders: number;
  availableResourceUnits: number;
  shelterCapacityTotal: number;
  shelterCapacityAvailable: number;
  recentIncidents: Incident[];
}

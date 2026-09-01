import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardSummary } from '../models/dashboard.model';
import { Incident } from '../models/incident.model';
import { PageResponse } from '../models/page.model';

interface IncidentSummary {
  total: number;
  active: number;
  critical: number;
}

interface ResponderSummary {
  total: number;
  active: number;
  available: number;
}

interface AvailabilityRatio {
  available: number;
  total: number;
  ratio: number | null;
}

interface ShelterCapacitySummary {
  capacityTotal: number;
  capacityOccupied: number;
  capacityAvailable: number;
}

/**
 * There is no dashboard-service in this architecture - aggregating a cross-service summary is
 * exactly what an API gateway/BFF layer is for, and doing it here (client-side, through the
 * gateway) keeps every backend service focused on owning one thing. Five parallel calls through
 * the gateway, combined once all resolve.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getSummary(): Observable<DashboardSummary> {
    const recentParams = new HttpParams().set('page', 0).set('size', 5).set('sort', 'reportedAt,desc');

    return forkJoin({
      incidents: this.http.get<IncidentSummary>(`${this.baseUrl}/incidents/summary`),
      responders: this.http.get<ResponderSummary>(`${this.baseUrl}/responders/summary`),
      resources: this.http.get<AvailabilityRatio>(`${this.baseUrl}/resources/availability-ratio`),
      shelters: this.http.get<ShelterCapacitySummary>(`${this.baseUrl}/shelters/summary`),
      recent: this.http.get<PageResponse<Incident>>(`${this.baseUrl}/incidents`, { params: recentParams }),
    }).pipe(
      map(({ incidents, responders, resources, shelters, recent }) => ({
        totalIncidents: incidents.total,
        activeIncidents: incidents.active,
        criticalIncidents: incidents.critical,
        activeResponders: responders.active,
        totalResponders: responders.total,
        availableResourceUnits: resources.available,
        shelterCapacityTotal: shelters.capacityTotal,
        shelterCapacityAvailable: shelters.capacityAvailable,
        recentIncidents: recent.content,
      })),
    );
  }
}

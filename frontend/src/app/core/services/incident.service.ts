import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Incident, IncidentRequest, IncidentStatus, PriorityTier, Severity } from '../models/incident.model';
import { PageResponse } from '../models/page.model';

export interface IncidentListFilters {
  status?: IncidentStatus;
  severity?: Severity;
  priorityTier?: PriorityTier;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/incidents`;

  list(filters: IncidentListFilters = {}): Observable<PageResponse<Incident>> {
    let params = new HttpParams();
    if (filters.status) params = params.set('status', filters.status);
    if (filters.severity) params = params.set('severity', filters.severity);
    if (filters.priorityTier) params = params.set('priorityTier', filters.priorityTier);
    params = params.set('page', filters.page ?? 0).set('size', filters.size ?? 20).set('sort', 'priorityScore,desc');

    return this.http.get<PageResponse<Incident>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Incident> {
    return this.http.get<Incident>(`${this.baseUrl}/${id}`);
  }

  create(request: IncidentRequest): Observable<Incident> {
    return this.http.post<Incident>(this.baseUrl, request);
  }

  update(id: number, request: IncidentRequest): Observable<Incident> {
    return this.http.put<Incident>(`${this.baseUrl}/${id}`, request);
  }

  recalculatePriority(id: number): Observable<Incident> {
    return this.http.post<Incident>(`${this.baseUrl}/${id}/recalculate-priority`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

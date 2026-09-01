import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Assignment, AssignmentRequest, AssignmentStatus } from '../models/assignment.model';
import { PageResponse } from '../models/page.model';

export interface AssignmentListFilters {
  incidentId?: number;
  status?: AssignmentStatus;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/assignments`;

  list(filters: AssignmentListFilters = {}): Observable<PageResponse<Assignment>> {
    let params = new HttpParams();
    if (filters.incidentId) params = params.set('incidentId', filters.incidentId);
    if (filters.status) params = params.set('status', filters.status);
    params = params.set('page', filters.page ?? 0).set('size', filters.size ?? 20);

    return this.http.get<PageResponse<Assignment>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Assignment> {
    return this.http.get<Assignment>(`${this.baseUrl}/${id}`);
  }

  create(request: AssignmentRequest): Observable<Assignment> {
    return this.http.post<Assignment>(this.baseUrl, request);
  }

  updateStatus(id: number, status: AssignmentStatus): Observable<Assignment> {
    return this.http.patch<Assignment>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

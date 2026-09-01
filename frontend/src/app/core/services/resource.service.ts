import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { EmergencyResource, ResourceRequest, ResourceStatus, ResourceType } from '../models/resource.model';

export interface ResourceListFilters {
  type?: ResourceType;
  status?: ResourceStatus;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/resources`;

  list(filters: ResourceListFilters = {}): Observable<PageResponse<EmergencyResource>> {
    let params = new HttpParams();
    if (filters.type) params = params.set('type', filters.type);
    if (filters.status) params = params.set('status', filters.status);
    params = params.set('page', filters.page ?? 0).set('size', filters.size ?? 20);

    return this.http.get<PageResponse<EmergencyResource>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<EmergencyResource> {
    return this.http.get<EmergencyResource>(`${this.baseUrl}/${id}`);
  }

  create(request: ResourceRequest): Observable<EmergencyResource> {
    return this.http.post<EmergencyResource>(this.baseUrl, request);
  }

  update(id: number, request: ResourceRequest): Observable<EmergencyResource> {
    return this.http.put<EmergencyResource>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

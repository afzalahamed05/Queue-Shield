import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Responder, ResponderRequest, ResponderRole, ResponderStatus } from '../models/responder.model';

export interface ResponderListFilters {
  role?: ResponderRole;
  status?: ResponderStatus;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ResponderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/responders`;

  list(filters: ResponderListFilters = {}): Observable<PageResponse<Responder>> {
    let params = new HttpParams();
    if (filters.role) params = params.set('role', filters.role);
    if (filters.status) params = params.set('status', filters.status);
    params = params.set('page', filters.page ?? 0).set('size', filters.size ?? 20);

    return this.http.get<PageResponse<Responder>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Responder> {
    return this.http.get<Responder>(`${this.baseUrl}/${id}`);
  }

  create(request: ResponderRequest): Observable<Responder> {
    return this.http.post<Responder>(this.baseUrl, request);
  }

  update(id: number, request: ResponderRequest): Observable<Responder> {
    return this.http.put<Responder>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Shelter, ShelterRequest, ShelterStatus } from '../models/shelter.model';

export interface ShelterListFilters {
  status?: ShelterStatus;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ShelterService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/shelters`;

  list(filters: ShelterListFilters = {}): Observable<PageResponse<Shelter>> {
    let params = new HttpParams();
    if (filters.status) params = params.set('status', filters.status);
    params = params.set('page', filters.page ?? 0).set('size', filters.size ?? 20);

    return this.http.get<PageResponse<Shelter>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Shelter> {
    return this.http.get<Shelter>(`${this.baseUrl}/${id}`);
  }

  create(request: ShelterRequest): Observable<Shelter> {
    return this.http.post<Shelter>(this.baseUrl, request);
  }

  update(id: number, request: ShelterRequest): Observable<Shelter> {
    return this.http.put<Shelter>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PriorityBreakdown } from '../models/priority.model';

@Injectable({ providedIn: 'root' })
export class PriorityService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/priorities`;

  getByIncidentId(incidentId: number): Observable<PriorityBreakdown> {
    return this.http.get<PriorityBreakdown>(`${this.baseUrl}/${incidentId}`);
  }
}

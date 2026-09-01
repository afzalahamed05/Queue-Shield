import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IncidentService } from '../../../core/services/incident.service';
import { Incident, IncidentStatus, PriorityTier, Severity, INCIDENT_STATUS_OPTIONS, SEVERITY_OPTIONS } from '../../../core/models/incident.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { priorityTierColor, severityColor, incidentStatusColor } from '../../../shared/badge-colors';

const PRIORITY_TIER_OPTIONS: PriorityTier[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink, BadgeComponent],
  templateUrl: './incident-list.component.html',
})
export class IncidentListComponent implements OnInit {
  private readonly incidentService = inject(IncidentService);

  readonly page = signal<PageResponse<Incident> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly statusOptions = INCIDENT_STATUS_OPTIONS;
  readonly severityOptions = SEVERITY_OPTIONS;
  readonly priorityTierOptions = PRIORITY_TIER_OPTIONS;

  status: IncidentStatus | '' = '';
  severity: Severity | '' = '';
  priorityTier: PriorityTier | '' = '';
  currentPage = 0;

  readonly priorityTierColor = priorityTierColor;
  readonly severityColor = severityColor;
  readonly incidentStatusColor = incidentStatusColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.incidentService
      .list({
        status: this.status || undefined,
        severity: this.severity || undefined,
        priorityTier: this.priorityTier || undefined,
        page: this.currentPage,
      })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load incidents.');
          this.loading.set(false);
        },
      });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.load();
  }

  goToPage(delta: number): void {
    this.currentPage += delta;
    this.load();
  }
}

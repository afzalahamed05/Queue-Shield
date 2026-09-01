import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IncidentService } from '../../../core/services/incident.service';
import { AssignmentService } from '../../../core/services/assignment.service';
import { PriorityService } from '../../../core/services/priority.service';
import { Incident } from '../../../core/models/incident.model';
import { Assignment } from '../../../core/models/assignment.model';
import { PriorityBreakdown } from '../../../core/models/priority.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { priorityTierColor, severityColor, incidentStatusColor, assignmentStatusColor } from '../../../shared/badge-colors';

/**
 * Priority is fetched from priority-service, a separate call from the incident itself - the two
 * services own genuinely different data now (see README's API boundaries section). Just after an
 * incident is created or reprioritization is requested, priority-service may not have caught up
 * yet (it reacts to a Kafka event asynchronously); this view treats a 404 from priority-service
 * as "still calculating" rather than an error.
 */
@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [DatePipe, RouterLink, BadgeComponent],
  templateUrl: './incident-detail.component.html',
})
export class IncidentDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly incidentService = inject(IncidentService);
  private readonly assignmentService = inject(AssignmentService);
  private readonly priorityService = inject(PriorityService);

  readonly incident = signal<Incident | null>(null);
  readonly priority = signal<PriorityBreakdown | null>(null);
  readonly priorityPending = signal(false);
  readonly assignments = signal<Assignment[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly priorityTierColor = priorityTierColor;
  readonly severityColor = severityColor;
  readonly incidentStatusColor = incidentStatusColor;
  readonly assignmentStatusColor = assignmentStatusColor;

  private id!: number;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.incidentService.getById(this.id).subscribe({
      next: (incident) => {
        this.incident.set(incident);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Incident not found.');
        this.loading.set(false);
      },
    });
    this.assignmentService.list({ incidentId: this.id, size: 50 }).subscribe({
      next: (page) => this.assignments.set(page.content),
    });
    this.loadPriority();
  }

  loadPriority(): void {
    this.priorityService.getByIncidentId(this.id).subscribe({
      next: (priority) => {
        this.priority.set(priority);
        this.priorityPending.set(false);
      },
      error: () => this.priorityPending.set(true),
    });
  }

  recalculatePriority(): void {
    this.incidentService.recalculatePriority(this.id).subscribe(() => {
      this.priorityPending.set(true);
      // priority-service reacts to the event asynchronously - give it a moment before re-reading.
      setTimeout(() => {
        this.loadPriority();
        this.incidentService.getById(this.id).subscribe((incident) => this.incident.set(incident));
      }, 1500);
    });
  }

  deleteIncident(): void {
    if (!confirm('Delete this incident? This cannot be undone.')) {
      return;
    }
    this.incidentService.delete(this.id).subscribe(() => this.router.navigate(['/incidents']));
  }
}

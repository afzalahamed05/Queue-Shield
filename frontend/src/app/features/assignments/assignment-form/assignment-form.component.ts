import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { AssignmentService } from '../../../core/services/assignment.service';
import { IncidentService } from '../../../core/services/incident.service';
import { ResponderService } from '../../../core/services/responder.service';
import { ResourceService } from '../../../core/services/resource.service';
import { ShelterService } from '../../../core/services/shelter.service';
import { Incident } from '../../../core/models/incident.model';
import { Responder } from '../../../core/models/responder.model';
import { EmergencyResource } from '../../../core/models/resource.model';
import { Shelter } from '../../../core/models/shelter.model';
import { ApiErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-assignment-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './assignment-form.component.html',
})
export class AssignmentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly assignmentService = inject(AssignmentService);
  private readonly incidentService = inject(IncidentService);
  private readonly responderService = inject(ResponderService);
  private readonly resourceService = inject(ResourceService);
  private readonly shelterService = inject(ShelterService);

  readonly saving = signal(false);
  readonly loadingOptions = signal(true);
  readonly error = signal<string | null>(null);

  readonly incidents = signal<Incident[]>([]);
  readonly responders = signal<Responder[]>([]);
  readonly resources = signal<EmergencyResource[]>([]);
  readonly shelters = signal<Shelter[]>([]);

  readonly form = this.fb.group({
    incidentId: this.fb.nonNullable.control<number | null>(null, Validators.required),
    responderId: this.fb.nonNullable.control<number | null>(null),
    resourceId: this.fb.nonNullable.control<number | null>(null),
    shelterId: this.fb.nonNullable.control<number | null>(null),
    notes: this.fb.nonNullable.control(''),
  });

  ngOnInit(): void {
    const incidentIdParam = this.route.snapshot.queryParamMap.get('incidentId');

    forkJoin({
      incidents: this.incidentService.list({ size: 100 }),
      responders: this.responderService.list({ status: 'AVAILABLE', size: 100 }),
      resources: this.resourceService.list({ size: 100 }),
      shelters: this.shelterService.list({ size: 100 }),
    }).subscribe(({ incidents, responders, resources, shelters }) => {
      this.incidents.set(incidents.content);
      this.responders.set(responders.content);
      this.resources.set(resources.content.filter((r) => r.quantityAvailable > 0));
      this.shelters.set(shelters.content);
      this.loadingOptions.set(false);

      if (incidentIdParam) {
        this.form.patchValue({ incidentId: Number(incidentIdParam) });
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    if (!raw.responderId && !raw.resourceId && !raw.shelterId) {
      this.error.set('Select at least one of responder, resource, or shelter.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    this.assignmentService
      .create({
        incidentId: raw.incidentId!,
        responderId: raw.responderId,
        resourceId: raw.resourceId,
        shelterId: raw.shelterId,
        notes: raw.notes || null,
      })
      .subscribe({
        next: (assignment) => {
          this.saving.set(false);
          this.router.navigate(['/incidents', assignment.incidentId]);
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          const body = err.error as ApiErrorResponse | undefined;
          this.error.set(body?.message ?? 'Failed to create assignment.');
        },
      });
  }
}

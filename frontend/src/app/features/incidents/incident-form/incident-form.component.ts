import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { IncidentService } from '../../../core/services/incident.service';
import { INCIDENT_STATUS_OPTIONS, SEVERITY_OPTIONS } from '../../../core/models/incident.model';
import { ApiErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-incident-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './incident-form.component.html',
})
export class IncidentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly incidentService = inject(IncidentService);

  readonly severityOptions = SEVERITY_OPTIONS;
  readonly statusOptions = INCIDENT_STATUS_OPTIONS;

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly isEditMode = signal(false);

  private incidentId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.maxLength(2000)]],
    location: ['', [Validators.required, Validators.maxLength(300)]],
    severity: ['MODERATE' as string, [Validators.required]],
    status: [''],
    peopleAffected: [0, [Validators.required, Validators.min(0)]],
    vulnerablePopulationCount: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.incidentId = Number(idParam);
      this.incidentService.getById(this.incidentId).subscribe((incident) => {
        this.form.patchValue({
          title: incident.title,
          description: incident.description ?? '',
          location: incident.location,
          severity: incident.severity,
          status: incident.status,
          peopleAffected: incident.peopleAffected,
          vulnerablePopulationCount: incident.vulnerablePopulationCount,
        });
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    if (raw.vulnerablePopulationCount > raw.peopleAffected) {
      this.error.set('Vulnerable population count cannot exceed people affected.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request = {
      title: raw.title,
      description: raw.description || null,
      location: raw.location,
      severity: raw.severity as any,
      status: (raw.status || null) as any,
      peopleAffected: raw.peopleAffected,
      vulnerablePopulationCount: raw.vulnerablePopulationCount,
    };

    const result = this.isEditMode()
      ? this.incidentService.update(this.incidentId!, request)
      : this.incidentService.create(request);

    result.subscribe({
      next: (incident) => {
        this.saving.set(false);
        this.router.navigate(['/incidents', incident.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiErrorResponse | undefined;
        this.error.set(body?.message ?? 'Failed to save incident.');
      },
    });
  }
}

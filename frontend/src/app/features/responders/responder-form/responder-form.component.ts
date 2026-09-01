import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ResponderService } from '../../../core/services/responder.service';
import { RESPONDER_ROLE_OPTIONS, RESPONDER_STATUS_OPTIONS } from '../../../core/models/responder.model';
import { ApiErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-responder-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './responder-form.component.html',
})
export class ResponderFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly responderService = inject(ResponderService);

  readonly roleOptions = RESPONDER_ROLE_OPTIONS;
  readonly statusOptions = RESPONDER_STATUS_OPTIONS;
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly isEditMode = signal(false);

  private responderId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    role: ['FIRE', [Validators.required]],
    phone: ['', [Validators.required, Validators.maxLength(30)]],
    status: ['AVAILABLE'],
    currentLocation: [''],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.responderId = Number(idParam);
      this.responderService.getById(this.responderId).subscribe((r) =>
        this.form.patchValue({ ...r, currentLocation: r.currentLocation ?? '' }),
      );
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const raw = this.form.getRawValue();
    const request = { ...raw, role: raw.role as any, status: raw.status as any, currentLocation: raw.currentLocation || null };

    const result = this.isEditMode()
      ? this.responderService.update(this.responderId!, request)
      : this.responderService.create(request);

    result.subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/responders']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiErrorResponse | undefined;
        this.error.set(body?.message ?? 'Failed to save responder.');
      },
    });
  }
}

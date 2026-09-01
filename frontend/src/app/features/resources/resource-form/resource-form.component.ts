import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ResourceService } from '../../../core/services/resource.service';
import { RESOURCE_TYPE_OPTIONS } from '../../../core/models/resource.model';
import { ApiErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-resource-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './resource-form.component.html',
})
export class ResourceFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly resourceService = inject(ResourceService);

  readonly typeOptions = RESOURCE_TYPE_OPTIONS;
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly isEditMode = signal(false);

  private resourceId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    type: ['MEDICAL_SUPPLIES', [Validators.required]],
    quantityTotal: [0, [Validators.required, Validators.min(0)]],
    quantityAvailable: [0, [Validators.required, Validators.min(0)]],
    location: ['', [Validators.required, Validators.maxLength(300)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.resourceId = Number(idParam);
      this.resourceService.getById(this.resourceId).subscribe((r) => this.form.patchValue(r));
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    if (raw.quantityAvailable > raw.quantityTotal) {
      this.error.set('Quantity available cannot exceed quantity total.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    const request = { ...raw, type: raw.type as any };

    const result = this.isEditMode()
      ? this.resourceService.update(this.resourceId!, request)
      : this.resourceService.create(request);

    result.subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/resources']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiErrorResponse | undefined;
        this.error.set(body?.message ?? 'Failed to save resource.');
      },
    });
  }
}

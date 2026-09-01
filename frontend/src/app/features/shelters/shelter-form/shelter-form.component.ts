import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ShelterService } from '../../../core/services/shelter.service';
import { ApiErrorResponse } from '../../../core/models/error-response.model';

@Component({
  selector: 'app-shelter-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './shelter-form.component.html',
})
export class ShelterFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly shelterService = inject(ShelterService);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly isEditMode = signal(false);

  private shelterId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    address: ['', [Validators.required, Validators.maxLength(300)]],
    capacityTotal: [0, [Validators.required, Validators.min(0)]],
    capacityOccupied: [0, [Validators.required, Validators.min(0)]],
    contactPhone: ['', [Validators.required, Validators.maxLength(30)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.shelterId = Number(idParam);
      this.shelterService.getById(this.shelterId).subscribe((s) => this.form.patchValue(s));
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    if (raw.capacityOccupied > raw.capacityTotal) {
      this.error.set('Capacity occupied cannot exceed capacity total.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const result = this.isEditMode()
      ? this.shelterService.update(this.shelterId!, raw)
      : this.shelterService.create(raw);

    result.subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/shelters']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiErrorResponse | undefined;
        this.error.set(body?.message ?? 'Failed to save shelter.');
      },
    });
  }
}

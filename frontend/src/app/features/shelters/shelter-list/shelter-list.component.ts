import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ShelterService } from '../../../core/services/shelter.service';
import { Shelter, ShelterStatus } from '../../../core/models/shelter.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { shelterStatusColor } from '../../../shared/badge-colors';

const SHELTER_STATUS_OPTIONS: ShelterStatus[] = ['OPEN', 'FULL', 'CLOSED'];

@Component({
  selector: 'app-shelter-list',
  standalone: true,
  imports: [FormsModule, RouterLink, BadgeComponent],
  templateUrl: './shelter-list.component.html',
})
export class ShelterListComponent implements OnInit {
  private readonly shelterService = inject(ShelterService);

  readonly page = signal<PageResponse<Shelter> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly statusOptions = SHELTER_STATUS_OPTIONS;

  status: ShelterStatus | '' = '';
  currentPage = 0;

  readonly shelterStatusColor = shelterStatusColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.shelterService.list({ status: this.status || undefined, page: this.currentPage }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load shelters.');
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

  deleteShelter(id: number, event: Event): void {
    event.stopPropagation();
    if (!confirm('Delete this shelter?')) return;
    this.shelterService.delete(id).subscribe(() => this.load());
  }
}

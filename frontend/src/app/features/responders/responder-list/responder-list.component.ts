import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ResponderService } from '../../../core/services/responder.service';
import { Responder, ResponderRole, ResponderStatus, RESPONDER_ROLE_OPTIONS, RESPONDER_STATUS_OPTIONS } from '../../../core/models/responder.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { responderStatusColor } from '../../../shared/badge-colors';

@Component({
  selector: 'app-responder-list',
  standalone: true,
  imports: [FormsModule, RouterLink, BadgeComponent],
  templateUrl: './responder-list.component.html',
})
export class ResponderListComponent implements OnInit {
  private readonly responderService = inject(ResponderService);

  readonly page = signal<PageResponse<Responder> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly roleOptions = RESPONDER_ROLE_OPTIONS;
  readonly statusOptions = RESPONDER_STATUS_OPTIONS;

  role: ResponderRole | '' = '';
  status: ResponderStatus | '' = '';
  currentPage = 0;

  readonly responderStatusColor = responderStatusColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.responderService
      .list({ role: this.role || undefined, status: this.status || undefined, page: this.currentPage })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load responders.');
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

  deleteResponder(id: number, event: Event): void {
    event.stopPropagation();
    if (!confirm('Delete this responder?')) return;
    this.responderService.delete(id).subscribe(() => this.load());
  }
}

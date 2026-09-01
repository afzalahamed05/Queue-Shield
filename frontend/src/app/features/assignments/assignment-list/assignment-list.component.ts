import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AssignmentService } from '../../../core/services/assignment.service';
import { Assignment, AssignmentStatus, ASSIGNMENT_STATUS_OPTIONS } from '../../../core/models/assignment.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { assignmentStatusColor } from '../../../shared/badge-colors';

@Component({
  selector: 'app-assignment-list',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink, BadgeComponent],
  templateUrl: './assignment-list.component.html',
})
export class AssignmentListComponent implements OnInit {
  private readonly assignmentService = inject(AssignmentService);

  readonly page = signal<PageResponse<Assignment> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly statusOptions = ASSIGNMENT_STATUS_OPTIONS;

  status: AssignmentStatus | '' = '';
  currentPage = 0;

  readonly assignmentStatusColor = assignmentStatusColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.assignmentService.list({ status: this.status || undefined, page: this.currentPage }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load assignments.');
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

  updateStatus(assignment: Assignment, status: AssignmentStatus): void {
    this.assignmentService.updateStatus(assignment.id, status).subscribe(() => this.load());
  }

  deleteAssignment(id: number): void {
    if (!confirm('Delete this assignment? Any dispatched responder/resource will be released.')) return;
    this.assignmentService.delete(id).subscribe(() => this.load());
  }
}

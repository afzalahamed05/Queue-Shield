import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ResourceService } from '../../../core/services/resource.service';
import { EmergencyResource, ResourceStatus, ResourceType, RESOURCE_TYPE_OPTIONS } from '../../../core/models/resource.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { resourceStatusColor } from '../../../shared/badge-colors';

const RESOURCE_STATUS_OPTIONS: ResourceStatus[] = ['AVAILABLE', 'LOW', 'DEPLETED', 'OUT_OF_SERVICE'];

@Component({
  selector: 'app-resource-list',
  standalone: true,
  imports: [FormsModule, RouterLink, BadgeComponent],
  templateUrl: './resource-list.component.html',
})
export class ResourceListComponent implements OnInit {
  private readonly resourceService = inject(ResourceService);

  readonly page = signal<PageResponse<EmergencyResource> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly typeOptions = RESOURCE_TYPE_OPTIONS;
  readonly statusOptions = RESOURCE_STATUS_OPTIONS;

  type: ResourceType | '' = '';
  status: ResourceStatus | '' = '';
  currentPage = 0;

  readonly resourceStatusColor = resourceStatusColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.resourceService
      .list({ type: this.type || undefined, status: this.status || undefined, page: this.currentPage })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load resources.');
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

  deleteResource(id: number, event: Event): void {
    event.stopPropagation();
    if (!confirm('Delete this resource?')) return;
    this.resourceService.delete(id).subscribe(() => this.load());
  }
}

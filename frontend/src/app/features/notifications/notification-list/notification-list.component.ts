import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';
import { PageResponse } from '../../../core/models/page.model';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { notificationTypeColor } from '../../../shared/badge-colors';

/**
 * Everything shown here originated as a Kafka event consumed by notification-service - this page
 * is a live view of what that service's fan-out consumers have picked up from priority-service,
 * assignment-service, resource-service, and shelter-service.
 */
@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [DatePipe, BadgeComponent],
  templateUrl: './notification-list.component.html',
})
export class NotificationListComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);

  readonly page = signal<PageResponse<Notification> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  currentPage = 0;

  readonly notificationTypeColor = notificationTypeColor;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.notificationService.list(this.currentPage).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load notifications.');
        this.loading.set(false);
      },
    });
  }

  goToPage(delta: number): void {
    this.currentPage += delta;
    this.load();
  }

  markRead(notification: Notification): void {
    if (notification.read) return;
    this.notificationService.markRead(notification.id).subscribe(() => this.load());
  }
}

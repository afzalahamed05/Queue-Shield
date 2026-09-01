import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-badge',
  standalone: true,
  template: `<span class="badge {{ colorClass }}">{{ text }}</span>`,
})
export class BadgeComponent {
  @Input({ required: true }) text = '';
  @Input() colorClass = 'badge-neutral';
}

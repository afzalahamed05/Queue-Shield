import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div class="card">
      <div class="stat-value" [style.color]="accentColor">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  `,
})
export class StatCardComponent {
  @Input({ required: true }) value: number | string = 0;
  @Input({ required: true }) label = '';
  @Input() accentColor = 'var(--color-text)';
}

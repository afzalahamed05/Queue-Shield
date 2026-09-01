import { Component, Input, OnChanges, SimpleChanges, signal } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div class="card stat-card">
      <div class="stat-value" [style.color]="accentColor">{{ display() }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  `,
  styles: [
    `
      .stat-card {
        text-align: center;
        position: relative;
        overflow: hidden;
      }
      .stat-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: var(--color-primary);
        opacity: 0.15;
      }
      .stat-card:hover {
        box-shadow: var(--shadow);
        transform: translateY(-2px);
      }
    `,
  ],
})
export class StatCardComponent implements OnChanges {
  @Input({ required: true }) value: number | string = 0;
  @Input({ required: true }) label = '';
  @Input() accentColor = 'var(--color-text)';

  readonly display = signal<string>('0');

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['value']) {
      return;
    }
    const target = this.value;
    if (typeof target !== 'number' || !Number.isFinite(target)) {
      this.display.set(String(target));
      return;
    }
    this.animateTo(target);
  }

  private animateTo(target: number): void {
    const durationMs = 600;
    const start = performance.now();
    const from = 0;

    const step = (now: number) => {
      const elapsed = now - start;
      const progress = Math.min(elapsed / durationMs, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = Math.round(from + (target - from) * eased);
      this.display.set(String(current));
      if (progress < 1) {
        requestAnimationFrame(step);
      }
    };
    requestAnimationFrame(step);
  }
}

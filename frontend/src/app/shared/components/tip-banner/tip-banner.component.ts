import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription, filter } from 'rxjs';
import { TIPS } from '../../data/tips';

/** Persistent strip, visible on every page, that rotates through real emergency-preparedness
 * and app-usage guidance - advances to the next tip on every route change (not on a timer),
 * so the content someone sees changes precisely because they navigated somewhere new. */
@Component({
  selector: 'app-tip-banner',
  standalone: true,
  templateUrl: './tip-banner.component.html',
  styleUrl: './tip-banner.component.css',
})
export class TipBannerComponent implements OnInit, OnDestroy {
  private readonly tips = TIPS;
  private index = Math.floor(Math.random() * this.tips.length);
  private subscription?: Subscription;

  readonly tip = signal(this.tips[this.index]);
  readonly fading = signal(false);

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.subscription = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.advance());
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  private advance(): void {
    this.fading.set(true);
    setTimeout(() => {
      this.index = (this.index + 1) % this.tips.length;
      this.tip.set(this.tips[this.index]);
      this.fading.set(false);
    }, 180);
  }
}

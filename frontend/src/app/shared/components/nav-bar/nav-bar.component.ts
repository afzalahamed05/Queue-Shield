import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.css',
})
export class NavBarComponent {
  readonly links = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/incidents', label: 'Incidents' },
    { path: '/resources', label: 'Resources' },
    { path: '/responders', label: 'Responders' },
    { path: '/shelters', label: 'Shelters' },
    { path: '/assignments', label: 'Assignments' },
    { path: '/notifications', label: 'Notifications' },
  ];
}

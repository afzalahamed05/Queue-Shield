import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'incidents',
    loadComponent: () =>
      import('./features/incidents/incident-list/incident-list.component').then((m) => m.IncidentListComponent),
  },
  {
    path: 'incidents/new',
    loadComponent: () =>
      import('./features/incidents/incident-form/incident-form.component').then((m) => m.IncidentFormComponent),
  },
  {
    path: 'incidents/:id',
    loadComponent: () =>
      import('./features/incidents/incident-detail/incident-detail.component').then(
        (m) => m.IncidentDetailComponent,
      ),
  },
  {
    path: 'incidents/:id/edit',
    loadComponent: () =>
      import('./features/incidents/incident-form/incident-form.component').then((m) => m.IncidentFormComponent),
  },
  {
    path: 'resources',
    loadComponent: () =>
      import('./features/resources/resource-list/resource-list.component').then((m) => m.ResourceListComponent),
  },
  {
    path: 'resources/new',
    loadComponent: () =>
      import('./features/resources/resource-form/resource-form.component').then((m) => m.ResourceFormComponent),
  },
  {
    path: 'resources/:id/edit',
    loadComponent: () =>
      import('./features/resources/resource-form/resource-form.component').then((m) => m.ResourceFormComponent),
  },
  {
    path: 'responders',
    loadComponent: () =>
      import('./features/responders/responder-list/responder-list.component').then(
        (m) => m.ResponderListComponent,
      ),
  },
  {
    path: 'responders/new',
    loadComponent: () =>
      import('./features/responders/responder-form/responder-form.component').then(
        (m) => m.ResponderFormComponent,
      ),
  },
  {
    path: 'responders/:id/edit',
    loadComponent: () =>
      import('./features/responders/responder-form/responder-form.component').then(
        (m) => m.ResponderFormComponent,
      ),
  },
  {
    path: 'shelters',
    loadComponent: () =>
      import('./features/shelters/shelter-list/shelter-list.component').then((m) => m.ShelterListComponent),
  },
  {
    path: 'shelters/new',
    loadComponent: () =>
      import('./features/shelters/shelter-form/shelter-form.component').then((m) => m.ShelterFormComponent),
  },
  {
    path: 'shelters/:id/edit',
    loadComponent: () =>
      import('./features/shelters/shelter-form/shelter-form.component').then((m) => m.ShelterFormComponent),
  },
  {
    path: 'assignments',
    loadComponent: () =>
      import('./features/assignments/assignment-list/assignment-list.component').then(
        (m) => m.AssignmentListComponent,
      ),
  },
  {
    path: 'assignments/new',
    loadComponent: () =>
      import('./features/assignments/assignment-form/assignment-form.component').then(
        (m) => m.AssignmentFormComponent,
      ),
  },
  {
    path: 'notifications',
    loadComponent: () =>
      import('./features/notifications/notification-list/notification-list.component').then(
        (m) => m.NotificationListComponent,
      ),
  },
  { path: '**', redirectTo: 'dashboard' },
];

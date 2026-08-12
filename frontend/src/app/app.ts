import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule,
    MatIconModule, MatSidenavModule, MatToolbarModule],
  template: `
    <mat-sidenav-container class="shell">
      <mat-sidenav mode="side" opened>
        <div class="brand">Gestão de Contratos</div>
        @for (item of navigation; track item.path) {
          <a mat-button [routerLink]="item.path" routerLinkActive="active">
            <mat-icon>{{ item.icon }}</mat-icon>{{ item.label }}
          </a>
        }
      </mat-sidenav>
      <mat-sidenav-content>
        <mat-toolbar color="primary">Controle de Contratos</mat-toolbar>
        <main><router-outlet /></main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .shell { min-height: 100vh; }
    mat-sidenav { width: 250px; padding: 1rem; }
    .brand { font-size: 1.1rem; font-weight: 600; padding: 1rem .5rem 1.5rem; }
    a { display: flex; justify-content: flex-start; margin-bottom: .25rem; width: 100%; }
    a.active { background: var(--mat-sys-secondary-container); }
    mat-icon { margin-right: .75rem; }
    main { padding: 1.5rem; }
  `]
})
export class App {
  protected readonly navigation = [
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/contracts', label: 'Contratos', icon: 'description' },
    { path: '/persons', label: 'Pessoas', icon: 'people' },
    { path: '/notifications', label: 'Notificações', icon: 'notifications' },
    { path: '/administration', label: 'Administração', icon: 'settings' }
  ];
}

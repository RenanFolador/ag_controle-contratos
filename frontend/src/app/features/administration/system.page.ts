import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { BACKEND_URL } from '../../core/config/api.config';

type HealthResponse = { status?: string };

@Component({
  selector: 'app-system-page',
  imports: [MatButtonModule, MatCardModule, MatIconModule, RouterLink],
  templateUrl: './system.page.html',
  styleUrl: './system.page.scss',
})
export class SystemPage {
  private readonly http = inject(HttpClient);
  private readonly backendUrl = inject(BACKEND_URL);

  readonly backendStatus = signal('Verificando...');
  readonly loading = signal(false);

  constructor() {
    this.checkHealth();
  }

  checkHealth(): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.http.get<HealthResponse>(`${this.backendUrl}/actuator/health`).subscribe({
      next: (response) => {
        this.backendStatus.set(response.status === 'UP' ? 'Disponível' : 'Indisponível');
        this.loading.set(false);
      },
      error: () => {
        this.backendStatus.set('Indisponível');
        this.loading.set(false);
      },
    });
  }
}
